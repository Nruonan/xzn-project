package com.example.listener;

import com.example.utils.Const;
import jakarta.annotation.Resource;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component

public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Value("${spring.mail.username}")
    String username;
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    /**
     * 处理邮件发送
     * @param data 邮件信息
     */

    @RabbitListener(queues = "mail")
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = data.get("email").toString();
        Integer code = (Integer) data.get("code");
        SimpleMailMessage message = switch (data.get("type").toString()) {
            case "register" -> createMessage("欢迎注册我们的网站",
                "您的邮件注册验证码为: " + code + "，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息。",
                email);
            case "reset" -> createMessage("您的密码重置邮件",
                "你好，您正在执行重置密码操作，验证码: " + code + "，有效时间3分钟，如非本人操作，请无视。",
                email);
            case "modify" -> createMessage("您的邮件修改验证邮件",
                "您好，您正在绑定新的电子邮件地址，验证码: " + code + "，有效时间3分钟，如非本人操作，请无视。",
                email);
            default -> null;
        };
        if (message == null) return;
        try {
            sender.send(message);
        }catch (Exception e){
            // 发送失败时进行重试
            int retryCount = (int) data.getOrDefault("retryCount", 0);
            if (retryCount < MAX_RETRY_COUNT) {
                // 更新重试次数并重新发送消息
                data.put("retryCount", retryCount + 1);
                rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);
            } else {
                // 达到最大重试次数时，记录日志或进行其他处理
                System.out.println("消息发送失败，超过最大重试次数");
            }
        }
    }

    /**
     * 快速封装简单邮件消息实体
     * @param title 标题
     * @param content 内容
     * @param email 收件人
     * @return 邮件实体
     */
    private SimpleMailMessage createMessage(String title, String content, String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}