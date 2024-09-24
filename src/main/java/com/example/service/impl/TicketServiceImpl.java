package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.controller.exception.ClientException;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.TicketDO;
import com.example.entity.dao.TicketOrderDO;
import com.example.entity.dao.TicketTypeDO;
import com.example.entity.dto.req.RemoveTicketOrderReqDTO;
import com.example.entity.dto.req.TicketOrderRepeatReqDO;
import com.example.entity.dto.req.TicketOrderReqDO;
import com.example.entity.dto.req.addTicketReqDTO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketOrderRespDTO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.entity.dto.resp.TicketTypeRespDTO;
import com.example.mapper.AccountMapper;
import com.example.mapper.TicketMapper;
import com.example.mapper.TicketOrderMapper;
import com.example.mapper.TicketTypeMapper;
import com.example.service.NotificationService;
import com.example.service.TicketService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Resource
    RBloomFilter<String> ticketBloomFilter;

    @Resource
    AccountMapper accountMapper;

    @Resource
    NotificationService notificationService;

    @Override
    public TicketRespDTO findTicketById(int id) {
        String key = Const.MARKET_TICKET_CACHE + ":" + id;
        TicketRespDTO ticketRespDTO = cacheUtils.takeFromCache(key, TicketRespDTO.class);
        TicketDO ticketDO = null;
        if (ticketRespDTO != null)return ticketRespDTO;
        if (!ticketBloomFilter.contains(String.valueOf(id))){
            throw new ClientException("神券不存在");
        }
        RLock lock = redissonClient.getLock("lock:" + key);
        if (!lock.tryLock()){
            throw new ClientException("正在获取神券中...");
        }
        try{
            ticketDO = baseMapper.selectById(id);
            if (Objects.isNull(ticketDO)){
                cacheUtils.saveToCache(key,new TicketRespDTO(),60);
                throw new ClientException("神券不存在或已过期！");
            }
            cacheUtils.saveToCache(key, BeanUtil.toBean(ticketDO, TicketRespDTO.class),60);
        }finally {
            lock.unlock();
        }

        return BeanUtil.toBean(ticketDO, TicketRespDTO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addTicket(addTicketReqDTO requestParam, int id) {
        AccountDO accountDO = accountMapper.selectById(id);
        if (!"admin".equals(accountDO.getRole())) {
            return "非管理员无法添加神券！";
        }
        if ((requestParam.getValidDateType() == 1  && requestParam.getValidDate() != null) || (requestParam.getValidDateType() == 0  && requestParam.getValidDate() == null)) {
            return "参数有误,请重新输入";
        }
        if (requestParam.getValidDate() != null && requestParam.getValidDate().before(new Date())) {
            return "参数有误,请重新输入";
        }
        if (requestParam.getType() == 1 || requestParam.getType() == 3){
            if (requestParam.getPrice() != 0) return "参数有误,请重新输入";
        }

        TicketDO bean = BeanUtil.toBean(requestParam, TicketDO.class);
        bean.setCreateTime(new Date());
        Date validDate = requestParam.getValidDate();
        // 将 Date 转换为 LocalDateTime
        LocalDateTime localDateTime = validDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        // 将时间倒退8个小时
        LocalDateTime newLocalDateTime = localDateTime.minusHours(8);
        // 将 LocalDateTime 转换回 Date
        Date newValidDate = Date.from(newLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // 设置新的时间
        bean.setValidDate(newValidDate);
        baseMapper.insert(bean);
        // 发送延时队列
        rabbitTemplate.convertAndSend("ticket_exchange", "add_ticket_exchange", bean.getId(), correlationData->{
            long expirationTime = newValidDate.getTime() - new Date().getTime();
            if (expirationTime > 0) {
                correlationData.getMessageProperties().setExpiration(String.valueOf(expirationTime));
            }
            return correlationData;
        });
        log.info("当前时间：{},发送一条时长{}毫秒 TTL 信息给队列 C:{}", new Date(),newValidDate.getTime() - new Date().getTime(), bean.getId());

        // 提醒大家可以有新的优惠券
        notificationService.addNotification();
        // 存入布隆过滤器
        ticketBloomFilter.add(String.valueOf(bean.getId()));
        return null;
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
    @Transactional(rollbackFor = Exception.class)
    public String saveTicketOrder(TicketOrderReqDO requestParam, int uid) {
        if (!requestParam.getUid().equals(uid) || this.findTicketById(requestParam.getTid()) == null){
            return "购买优惠券错误，请联系管理员！";
        }
        TicketRespDTO ticket = this.findTicketById(requestParam.getTid());
        if (ticket.getValidDateType() ==0 && ticket.getValidDate().before(new Date())){
            return "不满足神券购买时间";
        }
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
            if (ticket.getCount() <= 0 || requestParam.getCount() > ticket.getCount()) {
                return "神券已被购清，请下次参与活动";
            }

            TicketOrderDO ticketOrderDO = BeanUtil.toBean(requestParam, TicketOrderDO.class);
            ticketOrderDO.setTime(new Date());

            boolean isSuccess = ticketOrderMapper.insertOrUpdate(ticketOrderDO);

            ticket.setCount(ticket.getCount() - ticketOrderDO.getCount());
            baseMapper.updateById(BeanUtil.toBean(ticket,TicketDO.class));
            // 删除旧缓存
            cacheUtils.saveToCache(Const.MARKET_TICKET_CACHE + ":" + ticket.getId(),BeanUtil.toBean(ticket,TicketRespDTO.class),60);
            rabbitTemplate.convertAndSend("ticket_exchange", "save_order_exchange",requestParam.getId());
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
