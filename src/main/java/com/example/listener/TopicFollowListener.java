package com.example.listener;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.entity.dao.InboxTopicDO;
import com.example.mapper.InboxTopicMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Slf4j
@Component
public class TopicFollowListener {
    @Resource
    InboxTopicMapper inboxTopicMapper;

    @RabbitListener(queues = "topicFollowQueue")
    @RabbitHandler
    public void receive(List<InboxTopicDO> message) throws IOException {
        inboxTopicMapper.insert(message);
    }
}
