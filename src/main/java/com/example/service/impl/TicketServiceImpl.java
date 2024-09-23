package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.TicketDO;
import com.example.entity.dao.TicketOrderDO;
import com.example.entity.dao.TicketTypeDO;
import com.example.entity.dto.req.RemoveTicketOrderReqDTO;
import com.example.entity.dto.req.TicketOrderRepeatReqDO;
import com.example.entity.dto.req.TicketOrderReqDO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketOrderRespDTO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.entity.dto.resp.TicketTypeRespDTO;
import com.example.mapper.TicketMapper;
import com.example.mapper.TicketOrderMapper;
import com.example.mapper.TicketTypeMapper;
import com.example.service.TicketService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
@Slf4j
public class TicketServiceImpl extends ServiceImpl<TicketMapper, TicketDO> implements TicketService {

    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    CacheUtils cacheUtils;

    @Resource
    RedissonClient redissonClient;

    @Resource
    TicketTypeMapper ticketTypeMapper;

    @Resource
    TicketOrderMapper ticketOrderMapper;

    @Override
    public TicketRespDTO findTicketById(int id) {
        String key = Const.MARKET_TICKET_CACHE + ":" + id;
        TicketRespDTO ticketRespDTO = cacheUtils.takeFromCache(key, TicketRespDTO.class);
        if (ticketRespDTO != null)return ticketRespDTO;
        TicketDO ticketDO = baseMapper.selectById(id);
        if (ticketDO == null){
            cacheUtils.saveToCache(key,new TicketRespDTO(),60);
            return null;
        }
        cacheUtils.saveToCache(key,BeanUtil.toBean(ticketDO, TicketRespDTO.class),60);
        return BeanUtil.toBean(ticketDO, TicketRespDTO.class);
    }

    /**
     * @param name 优惠券名字
     * @param type 优惠券类型
     */
    @Override
    public TicketCountRespDTO ticketList(String name, int type,int pageNumber) {
        List<TicketRespDTO> ticketRespDTOS = this.ticketSearch(name, type, pageNumber);
        LambdaQueryWrapper<TicketDO> wrapper = null;
        if (name.length() == 0 && type == 0){
            wrapper = Wrappers.lambdaQuery(TicketDO.class)
                .orderByDesc(TicketDO::getCreateTime);
        }else if (name.length() != 0 && type ==0){
            wrapper = Wrappers.lambdaQuery(TicketDO.class)
                .like(TicketDO::getName,name)
                .select();
        }else if (name.length() == 0){
            wrapper = Wrappers.lambdaQuery(TicketDO.class)
                .eq(TicketDO::getType,type)
                .select();
        }else{
            wrapper = Wrappers.lambdaQuery(TicketDO.class)
                .eq(TicketDO::getType,type)
                .like(TicketDO::getName,name)
                .select();
        }
        Long aLong = baseMapper.selectCount(wrapper);
        return new TicketCountRespDTO(ticketRespDTOS,aLong);
    }

    private List<TicketRespDTO> ticketSearch(String name, int type, int pageNumber) {
        String key = Const.MARKET_TICKET_CACHE + pageNumber + ":" + type + ":" + name;
        List<TicketRespDTO> ticketRespDTOS = cacheUtils.takeListFormCache(key, TicketRespDTO.class);
        if (ticketRespDTOS != null)return ticketRespDTOS;
        String lockKey = "lock:" + key;
        RLock lock = redissonClient.getLock(lockKey);
        Page<TicketDO> page = new Page<>(pageNumber , 10);
        LambdaQueryWrapper<TicketDO> wrapper;
        try{
            if (!lock.tryLock()){
                ticketRespDTOS = cacheUtils.takeListFormCache(key, TicketRespDTO.class);
                if (ticketRespDTOS != null)return ticketRespDTOS;
            }
            if (name.length() == 0 && type == 0){
                wrapper = Wrappers.lambdaQuery(TicketDO.class)
                    .orderByDesc(TicketDO::getCreateTime);
            }else if (name.length() != 0 && type ==0){
                wrapper = Wrappers.lambdaQuery(TicketDO.class)
                    .like(TicketDO::getName,name)
                    .select();
            }else if (name.length() == 0){
                wrapper = Wrappers.lambdaQuery(TicketDO.class)
                    .eq(TicketDO::getType,type)
                    .select();
            }else{
                wrapper = Wrappers.lambdaQuery(TicketDO.class)
                    .eq(TicketDO::getType,type)
                    .like(TicketDO::getName,name)
                    .select();
            }
            baseMapper.selectPage(page,wrapper);
            List<TicketDO> records = page.getRecords();
            ticketRespDTOS = BeanUtil.copyToList(records, TicketRespDTO.class);
            if (records.isEmpty()){
                cacheUtils.saveListToCache(key,ticketRespDTOS,60);
                return new ArrayList<>();
            }
            cacheUtils.saveListToCache(key,ticketRespDTOS,60);
        }finally {
            lock.unlock();
        }
        return ticketRespDTOS;
    }


    @Override
    public List<TicketTypeRespDTO> ticketTypeList() {
        List<TicketTypeDO> ticketTypeDOS = ticketTypeMapper.selectList(null);
        return BeanUtil.copyToList(ticketTypeDOS,TicketTypeRespDTO.class);
    }

    @Override
    public String saveTicketOrder(TicketOrderReqDO requestParam, int uid) {
        if (!requestParam.getUid().equals(uid) || this.findTicketById(requestParam.getTid()) == null){
            return "购买优惠券错误，请联系管理员！";
        }
        TicketRespDTO ticket = this.findTicketById(requestParam.getTid());
        if (ticket.getPrice() != requestParam.getPrice() / requestParam.getCount()){
            return "支付金额出错，请联系管理员！";
        }
        String key = Const.MARKET_TICKET_PAY + ":" +requestParam.getId();
        RLock lock = redissonClient.getLock(key);
        if (!lock.tryLock()){
            return "请进入购买界面，退出后重新进入";
        }
        try{
            ticket = this.findTicketById(requestParam.getTid());
            TicketOrderDO ticketOrderDO = BeanUtil.toBean(requestParam, TicketOrderDO.class);
            ticketOrderDO.setTime(new Date());

            boolean isSuccess = ticketOrderMapper.insertOrUpdate(ticketOrderDO);
            ticket.setCount(ticket.getCount() - ticketOrderDO.getCount());
            baseMapper.updateById(BeanUtil.toBean(ticket,TicketDO.class));
            // 删除旧缓存
            cacheUtils.deleteCache(Const.MARKET_TICKET_CACHE + ":" + ticket.getId());
            rabbitTemplate.convertAndSend("X", "XC", Const.MARKET_TICKET_PAY , correlationData ->{
                correlationData.getMessageProperties().setExpiration("901000");
                correlationData.getMessageProperties().setMessageId(requestParam.getId().toString());
                return correlationData;
            });
            log.info("当前时间：{},发送一条时长{}毫秒 TTL 信息给队列 C:{}", new Date(),"901000", requestParam.getId());
            if (isSuccess){
                return null;
            }else {
                return "请重新尝试购买";
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public String saveTicketOrderRepeat(TicketOrderRepeatReqDO requestParam) {
        String key = Const.MARKET_TICKET_PAY + ":" +requestParam.getId();
        RLock lock = redissonClient.getLock(key);
        if (!lock.tryLock()){
            return "请进入购买界面，退出后重新进入";
        }
        try{
            TicketOrderDO ticketOrderDO = BeanUtil.toBean(requestParam, TicketOrderDO.class);
            int isSuccess = ticketOrderMapper.updateById(ticketOrderDO);
            if (isSuccess > 0){
                return null;
            }else {
                return "请重新尝试购买";
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public List<TicketOrderRespDTO> getTicketOrdersById(int id, int uid) {
        if (id != uid){
            return null;
        }

        return ticketOrderMapper.selectOrdersList(uid);
    }

    @Override
    public String removeTicketOrder(int id, RemoveTicketOrderReqDTO requestParam) {
        if (id != requestParam.getUid()){
            return "删除订单错误，请联系管理员！";
        }
        int i = ticketOrderMapper.deleteById(requestParam.getId());
        if (i > 0){
            return null;
        }
        return "删除订单错误，请联系管理员！";
    }
}
