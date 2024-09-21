package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.RestBean;
import com.example.entity.dao.TicketDO;
import com.example.entity.dao.TicketTypeDO;
import com.example.entity.dao.TopicDO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.entity.dto.resp.TicketTypeRespDTO;
import com.example.mapper.TicketMapper;
import com.example.mapper.TicketTypeMapper;
import com.example.service.TicketService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.Lambda;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, TicketDO> implements TicketService {

    @Resource
    CacheUtils cacheUtils;

    @Resource
    RedissonClient redissonClient;

    @Resource
    TicketTypeMapper ticketTypeMapper;

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
}
