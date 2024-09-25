package com.example.config;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nruonan
 * @description
 */
@Configuration
public class RabbitConfiguration {

    @Bean("mailQueue")
    public Queue queue(){
        return QueueBuilder
            .durable("mail")
            .build();
    }

    @Bean("notificationTicketQueue")
    public Queue nQueue(){
        return QueueBuilder
            .durable("notificationTicket")
            .build();
    }

    @Bean("notificationCommentQueue")
    public Queue cQueue(){
        return QueueBuilder
            .durable("notificationComment")
            .build();
    }

    @Bean("ticket_count_queue")
    public Queue ticketQueue(){
        return QueueBuilder
            .durable("delete_ticket_count_queue")
            .build();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }

    public static final String X_EXCHANGE = "ticket_exchange";
    public static final String QUEUE_A = "save_order_queue";
    public static final String QUEUE_B = "add_ticket_queue";
    public static final String Y_DEAD_LETTER_EXCHANGE = "delay_save_order_exchange";
    public static final String DEAD_LETTER_QUEUED = "delay_save_order_queue";
    public static final String Z_DEAD_LETTER_EXCHANGE = "delay_add_ticket_exchange";
    public static final String DEAD_LETTER_QUEUEC = "delay_add_ticket_queue";

    // 声明 xExchange
    @Bean("xExchange")
    public DirectExchange xExchange(){
        return new DirectExchange(X_EXCHANGE);
    }
    // 声明 xExchange
    @Bean("yExchange")
    public DirectExchange yExchange(){
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    // 声明 xExchange
    @Bean("zExchange")
    public DirectExchange zExchange(){
        return new DirectExchange(Z_DEAD_LETTER_EXCHANGE);
    }
    //声明队列 A ttl 为 10s 并绑定到对应的死信交换机
    @Bean("queueA")
    public Queue queueA(){
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        //声明队列的 TTL
        args.put("x-message-ttl", 901000);
        return QueueBuilder.durable(QUEUE_A).withArguments(args).build();
    }
    // 声明队列 A 绑定 X 交换机
    @Bean
    public Binding queueaBindingX(@Qualifier("queueA") Queue queueA,@Qualifier("xExchange") DirectExchange xExchange){
        return BindingBuilder.bind(queueA).to(xExchange).with("save_order_exchange");
    }

    //声明队列 B ttl 为 40s 并绑定到对应的死信交换机
    @Bean("queueB")
    public Queue queueB(){
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Z_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YC");
        return QueueBuilder.durable(QUEUE_B).withArguments(args).build();
    }

    //声明队列 B 绑定 X 交换机
    @Bean
    public Binding queuebBindingX(@Qualifier("queueB") Queue queue1B,@Qualifier("xExchange") DirectExchange xExchange){
        return BindingBuilder.bind(queue1B).to(xExchange).with("add_ticket_exchange");
    }
    //声明死信队列 QD
    @Bean("queueD")
    public Queue queueD(){
        return new Queue(DEAD_LETTER_QUEUED);
    }
    //声明死信队列 QD 绑定关系
    @Bean
    public Binding deadLetterBindingQAD(@Qualifier("queueD") Queue queueD,@Qualifier("yExchange") DirectExchange yExchange){
        return BindingBuilder.bind(queueD).to(yExchange).with("YD");
    }
    @Bean("queueC")
    public Queue queueC(){
        return new Queue(DEAD_LETTER_QUEUEC);
    }
    //声明死信队列 QD 绑定关系
    @Bean
    public Binding deadLetterBindingQAC(@Qualifier("queueC") Queue queueC,@Qualifier("zExchange") DirectExchange zExchange){
        return BindingBuilder.bind(queueC).to(zExchange).with("YC");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}