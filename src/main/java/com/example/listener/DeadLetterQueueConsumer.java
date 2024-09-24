package com.example.listener;

import com.example.mapper.TicketMapper;
import com.example.mapper.TicketOrderMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DeadLetterQueueConsumer {

    @Resource
    TicketOrderMapper orderMapper;

    @Resource
    TicketMapper ticketMapper;
    @RabbitListener(queues = "delay_save_order_queue")
    @Transactional(rollbackFor = Exception.class)
    public void receiveD(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());

        int id = Integer.parseInt(msg);
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
}