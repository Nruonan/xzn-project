package com.example.listener;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dao.TicketDO;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.NotificationService;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component
public class NotificationListener {

    @Resource
    AccountPrivacyMapper privacyMapper;

    @Resource
    NotificationService notificationService;
    @RabbitListener(queues = "notification")
    @RabbitHandler
    public void sendMessage(String  s) {
        TicketDO ticketDO = JSONObject.parseObject(s, TicketDO.class);
        List<AccountPrivacyDO> accountList = privacyMapper.selectList(Wrappers.<AccountPrivacyDO>query().eq("remind", 1));
        for (AccountPrivacyDO account : accountList){
            notificationService.addNotification(account.getId(),"您有新的神券预约提醒",
                ticketDO.getName() +" " +ticketDO.getDesc() +"，快去看看吧！","success",
                "/index/market/ticket-order/" + ticketDO.getId());
        }
    }
}
