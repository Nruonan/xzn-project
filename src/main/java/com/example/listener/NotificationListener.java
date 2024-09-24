package com.example.listener;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dao.TicketDO;
import com.example.entity.dao.TopicCommentDO;
import com.example.entity.dao.TopicDO;
import com.example.entity.dto.req.AddCommentReqDTO;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.mapper.TopicMapper;
import com.example.service.NotificationService;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    AccountMapper accountMapper;

    @Resource
    TopicMapper topicMapper;

    @Resource
    NotificationService notificationService;
    @RabbitListener(queues = "notificationTicket")
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

    @RabbitListener(queues = "notificationComment")
    @RabbitHandler
    public void sendMessageB(HashMap<String,String> map) {
        TopicCommentDO bean = JSONObject.parseObject(map.get("bean"), TopicCommentDO.class);
        AddCommentReqDTO requestParam = JSONObject.parseObject(map.get("req"), AddCommentReqDTO.class);
        TopicDO topicDO = topicMapper.selectById(requestParam.getTid());
        AccountDO accountDO = accountMapper.selectById(bean.getUid());
        if (requestParam.getQuote() > 0){
            if (!Objects.equals(accountDO.getId(),requestParam.getQuote())){
                notificationService.addNotification(requestParam.getQuote(),"您有新的帖子评论回复",
                    accountDO.getUsername() + "回复了你发表的评论，快去看看吧!",
                    "success","/index/topic-detail/" + bean.getTid());
            }
        }else if(!Objects.equals(accountDO.getId(),topicDO.getUid())){
            notificationService.addNotification(topicDO.getUid(),"您有新的帖子评论回复",
                accountDO.getUsername() + "回复了你发表的主题: "+topicDO.getTitle()+"，快去看看吧!",
                "success","/index/topic-detail/" + bean.getTid());
        }
    }
}
