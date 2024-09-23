package com.example.listener;

import com.example.mapper.TicketOrderMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeadLetterQueueConsumer {

    @Resource
    TicketOrderMapper orderMapper;
    @RabbitListener(queues = "QD")
    public void receiveD(Message message, Channel channel) throws IOException {
        String msg = new String(message.getBody());
        String messageId = message.getMessageProperties().getMessageId();
        orderMapper.updateTicketCount(Long.parseLong(messageId));
        orderMapper.deleteTicketOrder(Long.parseLong(messageId));
        log.info("当前时间：{},收到死信队列信息{}", new Date().toString(), msg);
    }
}