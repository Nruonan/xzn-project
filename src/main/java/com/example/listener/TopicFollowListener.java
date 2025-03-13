package com.example.listener;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.entity.dao.FollowDO;
import com.example.entity.dao.InboxTopicDO;
import com.example.entity.dao.TopicDO;
import com.example.mapper.FollowMapper;
import com.example.mapper.InboxTopicMapper;
import com.example.mapper.TopicMapper;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Slf4j
@Component
public class TopicFollowListener {
    @Resource
    TopicMapper topicMapper;
    @Resource
    FollowMapper followMapper;
    @Resource
    CacheUtils cacheUtils;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @RabbitListener(queues = "topic_follow_queue")
    @RabbitHandler
    public void receive(TopicDO topic,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // 业务处理逻辑
            System.out.println("处理消息: " + topic);
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            Set<String> followSet = stringRedisTemplate.opsForZSet().range(Const.FOLLOW_CACHE + topic.getUid(), 0, -1);
            if (followSet.isEmpty()){
                followSet = followMapper.selectList(new LambdaQueryWrapper<>(FollowDO.class)
                    .eq(FollowDO::getFid, topic.getUid())).stream().map(followDO -> String.valueOf(followDO.getUid())).collect(Collectors.toSet());
            }
            // 如果是大V用户 发送自身邮箱
            if (followSet.size() >= 5) {
//            InboxTopicDO build = InboxTopicDO.builder()
//                .tid(topic.getId())
//                .fid(topic.getUid())
//                .uid(topic.getUid())
//                .time(topic.getTime())
//                .type(topic.getType())
//                .title(topic.getTitle())
//                .content(topic.getContent())
//                .build();
//                        inboxTopicMapper.insert(build);
                stringRedisTemplate.opsForZSet().add(Const.FEED_BIG_CACHE + topic.getUid(),String.valueOf(topic.getId()),System.currentTimeMillis());
            } else {
//            List<InboxTopicDO> list = new ArrayList<>();
//            followDOS.forEach(followDO -> {
//                InboxTopicDO inboxTopicDO = InboxTopicDO.builder()
//                    .uid(followDO.getUid())
//                    .fid(topic.getUid())
//                    .tid(topic.getId())
//                    .title(topic.getTitle())
//                    .content(topic.getContent())
//                    .type(topic.getType())
//                    .time(topic.getTime())
//                    .build();
//                list.add(inboxTopicDO);
                followSet.stream().forEach(fid -> {
                    String key = Const.FEED_CACHE + fid;
                    stringRedisTemplate.opsForZSet().add(key,String.valueOf(topic.getId()),System.currentTimeMillis());
                });
            }
            // 手动确认消息
            channel.basicAck(tag, false);
        } catch (Exception e) {
            // 处理失败，拒绝消息（可配置重试或进入死信队列）
            channel.basicNack(tag, false, true);
        }

    }

    @RabbitListener(queues = "follow_queue")
    @RabbitHandler
    public String  receiveB(HashMap<String,Object> map){
        String key = map.get("key").toString();
        int count = (int) map.get("count");
        FollowDO followDO = BeanUtil.toBean(map.get("follow"), FollowDO.class);

        int id = (int) map.get("id");
        int uid = (int) map.get("uid");
        if (followDO == null){
            if(count >= 5)return "关注失败，最多只能关注5个用户";
            followDO = new FollowDO();
            followDO.setFid(id);
            followDO.setUid(uid);
            followDO.setStatus(1);
            followDO.setTime(new Date());
            sendInbox(id,uid);
            int isSuccess = followMapper.insert(followDO);

            if (isSuccess > 0){
                stringRedisTemplate.opsForZSet().add(key, String.valueOf(id),System.currentTimeMillis());
            }

        }else {
            if (followDO.getStatus() == 1) {
                LambdaUpdateWrapper<FollowDO> updateWrapper = new LambdaUpdateWrapper<>(FollowDO.class)
                    .eq(FollowDO::getUid, uid)
                    .eq(FollowDO::getFid,id)
                    .set(FollowDO::getStatus,0);
                followMapper.update(updateWrapper);
                pullInbox(id,uid);
                stringRedisTemplate.opsForZSet().remove(key, String.valueOf(id));
            } else {
                if(count >= 5)return "关注失败，最多只能关注5个用户";
                LambdaUpdateWrapper<FollowDO> updateWrapper = new LambdaUpdateWrapper<>(FollowDO.class)
                    .eq(FollowDO::getUid, uid)
                    .eq(FollowDO::getFid,id)
                    .set(FollowDO::getStatus,1);
                followMapper.update(updateWrapper);
                sendInbox(id,uid);
                stringRedisTemplate.opsForZSet().add(key, String.valueOf(id),System.currentTimeMillis());
            }
        }
        return null;
    }

    private void pullInbox(int id,int uid){
        List<TopicDO> topicDOS = topicMapper.selectList(new LambdaQueryWrapper<>(TopicDO.class)
            .eq(TopicDO::getUid, id));
        topicDOS.forEach(topic ->{
            stringRedisTemplate.opsForZSet().remove(Const.FEED_CACHE + uid,String.valueOf(topic.getId()));
        });
//        inboxTopicMapper.delete(new LambdaQueryWrapper<>(InboxTopicDO.class)
//            .eq(InboxTopicDO::getFid, id)
//            .eq(InboxTopicDO::getUid, uid));
    }
    public void sendInbox(int id,int uid){
        List<TopicDO> topicDOS = topicMapper.selectList(new LambdaQueryWrapper<>(TopicDO.class)
            .eq(TopicDO::getUid, id))
            .stream().filter(topicDO -> topicDO.getTime().after(DateUtil.offsetDay(new Date(),-14))).toList();
//        List<InboxTopicDO> inboxTopicDOS = new ArrayList<>();
//        Date now = new Date();
//        Date date = DateUtil.offsetDay(now,-7);
//
//        for (TopicDO topicDO : topicDOS) {
//            if(topicDO.getTime().before(date))continue;
//            InboxTopicDO inboxTopicDO = InboxTopicDO.builder()
//                .uid(uid)
//                .fid(id)
//                .tid(topicDO.getId())
//                .type(topicDO.getType())
//                .content(topicDO.getContent())
//                .title(topicDO.getTitle())
//                .time(topicDO.getTime())
//                .build();
//            inboxTopicDOS.add(inboxTopicDO);
//        }
        topicDOS.forEach(topic ->{
            stringRedisTemplate.opsForZSet().add(Const.FEED_CACHE + uid,topic.getId().toString(),System.currentTimeMillis());
        });

//        inboxTopicMapper.insert(inboxTopicDOS);
    }
}
