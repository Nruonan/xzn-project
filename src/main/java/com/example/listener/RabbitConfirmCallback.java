package com.example.listener;

import jakarta.annotation.PostConstruct;
import java.util.Objects;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;


@Component
public class RabbitConfirmCallback implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private final RabbitTemplate rabbitTemplate;

    public RabbitConfirmCallback(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {
        // 设置确认回调和返回回调
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("消息成功到达交换机，ID: " + correlationData.getId());
        } else {
            System.err.println("消息未到达交换机，原因: " + cause);
            int i = 3;
            while(i != 0){
                System.out.println("消息重新发送，剩余次数: " + i);
                rabbitTemplate.convertAndSend(Objects.requireNonNull(correlationData.getReturned()).getExchange(), correlationData.getReturned().getRoutingKey(), correlationData.getId());
                i--;
            }
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        System.err.println("消息无法路由到队列，触发回退！");
        System.err.println("消息主体: " + new String(returned.getMessage().getBody()));
        System.err.println("回应码: " + returned.getReplyCode());
        System.err.println("回应信息: " + returned.getReplyText());
        System.err.println("交换机: " + returned.getExchange());
        System.err.println("路由键: " + returned.getRoutingKey());
    }
}