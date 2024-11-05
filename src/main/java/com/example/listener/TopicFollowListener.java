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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Resource
    TopicMapper topicMapper;
    @Resource
    FollowMapper followMapper;
    @Resource
    CacheUtils cacheUtils;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @RabbitListener(queues = "topicFollowQueue")
    @RabbitHandler
    public void receive(TopicDO topic){
        cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
        List<FollowDO> followDOS = followMapper.selectList(new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getFid, topic.getUid()));
        // 如果是大V用户 发送自身邮箱
        if (followDOS.size() >= 5) {
            InboxTopicDO build = InboxTopicDO.builder()
                .tid(topic.getId())
                .fid(topic.getUid())
                .uid(topic.getUid())
                .time(topic.getTime())
                .type(topic.getType())
                .title(topic.getTitle())
                .content(topic.getContent())
                .build();
            inboxTopicMapper.insert(build);
        } else {
            List<InboxTopicDO> list = new ArrayList<>();
            followDOS.forEach(followDO -> {
                InboxTopicDO inboxTopicDO = InboxTopicDO.builder()
                    .uid(followDO.getUid())
                    .fid(topic.getUid())
                    .tid(topic.getId())
                    .title(topic.getTitle())
                    .content(topic.getContent())
                    .type(topic.getType())
                    .time(topic.getTime())
                    .build();
                list.add(inboxTopicDO);
            });
            inboxTopicMapper.insert(list);
        }
    }

    @RabbitListener(queues = "FollowQueue")
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
                stringRedisTemplate.opsForSet().add(key, String.valueOf(id));
            }

        }else {
            if (followDO.getStatus() == 1) {
                LambdaUpdateWrapper<FollowDO> updateWrapper = new LambdaUpdateWrapper<>(FollowDO.class)
                    .eq(FollowDO::getUid, uid)
                    .eq(FollowDO::getFid,id)
                    .set(FollowDO::getStatus,0);
                followMapper.update(updateWrapper);
                pullInbox(id,uid);
                stringRedisTemplate.opsForSet().remove(key, String.valueOf(id));
            } else {
                if(count >= 5)return "关注失败，最多只能关注5个用户";
                LambdaUpdateWrapper<FollowDO> updateWrapper = new LambdaUpdateWrapper<>(FollowDO.class)
                    .eq(FollowDO::getUid, uid)
                    .eq(FollowDO::getFid,id)
                    .set(FollowDO::getStatus,1);
                followMapper.update(updateWrapper);
                sendInbox(id,uid);
                stringRedisTemplate.opsForSet().add(key, String.valueOf(id));
            }
        }
        return null;
    }

    private void pullInbox(int id,int uid){
        inboxTopicMapper.delete(new LambdaQueryWrapper<>(InboxTopicDO.class)
            .eq(InboxTopicDO::getFid, id)
            .eq(InboxTopicDO::getUid, uid));
    }
    public void sendInbox(int id,int uid){
        List<TopicDO> topicDOS = topicMapper.selectList(new LambdaQueryWrapper<>(TopicDO.class)
            .eq(TopicDO::getUid, id));
        List<InboxTopicDO> inboxTopicDOS = new ArrayList<>();
        Date now = new Date();
        Date date = DateUtil.offsetDay(now,-7);

        for (TopicDO topicDO : topicDOS) {
            if(topicDO.getTime().before(date))continue;
            InboxTopicDO inboxTopicDO = InboxTopicDO.builder()
                .uid(uid)
                .fid(id)
                .tid(topicDO.getId())
                .type(topicDO.getType())
                .content(topicDO.getContent())
                .title(topicDO.getTitle())
                .time(topicDO.getTime())
                .build();
            inboxTopicDOS.add(inboxTopicDO);
        }

        inboxTopicMapper.insert(inboxTopicDOS);
    }
}
