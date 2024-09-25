package com.example.listener;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.dao.TicketDO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.mapper.TicketMapper;
import com.example.mapper.TicketOrderMapper;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nruonan
 */
@Slf4j
@Component
public class TicketQueueListener {

    @Resource
    TicketOrderMapper orderMapper;
    @Resource
    CacheUtils cacheUtils;
    @Resource
    TicketMapper ticketMapper;
    @RabbitListener(queues = "delay_save_order_queue")
    @Transactional(rollbackFor = Exception.class)
    public void receiveD(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());

        long id = Long.parseLong(msg);
        orderMapper.updateTicketCount(id);
        orderMapper.deleteTicketOrder(id);

        log.info("当前时间：{},收到死信队列信息{}", new Date(), msg);
    }

    @RabbitListener(queues = "delay_add_ticket_queue")
    @Transactional(rollbackFor = Exception.class)
    public void receiveC(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        int id = Integer.parseInt(msg);
        ticketMapper.deleteById(id);
        log.info("当前时间：{},收到死信队列信息{}", new Date(), msg);
    }

    @RabbitListener(queues = "delete_ticket_count_queue")
    @Transactional(rollbackFor = Exception.class)
    @Retryable(value = { Exception.class },maxAttemptsExpression = "${spring.rabbit.listener.simple.retry.max-attempts}",
        backoff = @Backoff(delayExpression = "${spring.rabbit.listener.simple.retry.back-off.delay}"))
    public void receiveOrder(String id){
        cacheUtils.deleteCache(Const.MARKET_TICKET_CACHE + ":" + id);
        log.info("当前时间：{},删除缓存队列信息{}", new Date(), id);
    }
}