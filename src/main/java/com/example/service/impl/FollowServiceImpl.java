package com.example.service.impl;

import static com.example.utils.Const.FOLLOW_CACHE;


import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.FollowDO;
import com.example.entity.dao.InboxTopicDO;
import com.example.entity.dao.TopicDO;
import com.example.mapper.FollowMapper;
import com.example.mapper.InboxTopicMapper;
import com.example.mapper.TopicMapper;
import com.example.service.FollowService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nruonan
 * @description
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, FollowDO> implements FollowService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    TopicMapper topicMapper;

    @Resource
    InboxTopicMapper inboxTopicMapper;
    @Override
    public boolean isFollow(int id, int uid) {
        Long count = query().eq("uid", uid)
            .eq("fid", id).eq("status",1).count();
        //只想知道有没有，所以用count(*)即可
        return count > 0;
    }


    @Override
    @Transactional
    public String followById(int id,int uid) {
        if (id == uid)return "关注错误，关注用户为当前用户！";
        String key = FOLLOW_CACHE + uid;
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, uid));

        LambdaQueryWrapper<FollowDO> eq = new LambdaQueryWrapper<>(FollowDO.class)
            .in(FollowDO::getUid, uid)
            .eq(FollowDO::getFid,id);
        FollowDO followDO = baseMapper.selectOne(eq);
        if (followDO == null){
            if(count >= 5)return "关注失败，最多只能关注5个用户";
            followDO = new FollowDO();
            followDO.setFid(id);
            followDO.setUid(uid);
            followDO.setStatus(1);
            followDO.setTime(new Date());
            sendInbox(id,uid);
            boolean isSuccess = save(followDO);

            if (isSuccess){
                stringRedisTemplate.opsForSet().add(key, String.valueOf(id));
            }

        }else {
            if (followDO.getStatus() == 1) {
                LambdaUpdateWrapper<FollowDO> updateWrapper = new LambdaUpdateWrapper<>(FollowDO.class)
                    .eq(FollowDO::getUid, uid)
                    .eq(FollowDO::getFid,id)
                    .set(FollowDO::getStatus,0);
                update(updateWrapper);
                pullInbox(id,uid);
                stringRedisTemplate.opsForSet().remove(key, String.valueOf(id));
            } else {
                if(count >= 5)return "关注失败，最多只能关注5个用户";
                LambdaUpdateWrapper<FollowDO> updateWrapper = new LambdaUpdateWrapper<>(FollowDO.class)
                    .eq(FollowDO::getUid, uid)
                    .eq(FollowDO::getFid,id)
                    .set(FollowDO::getStatus,1);
                update(updateWrapper);
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
    @Override
    public List<Integer> followList(int uid) {
        LambdaQueryWrapper<FollowDO> eq = new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, uid).eq(FollowDO::getStatus,1);
        List<FollowDO> followDOS = baseMapper.selectList(eq);
        List<Integer> collect = followDOS.stream()
            .map(FollowDO::getFid)
            .toList();
        return collect;
    }

    @Override
    public Integer findFansById(int id) {
        Long fans = baseMapper.selectCount(new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getFid, id).eq(FollowDO::getStatus,1));
        if(fans > 0){
            return Math.toIntExact(fans);
        }
        return 0;
    }

    @Override
    public Integer findFollowsById(int id) {
        Long follows = baseMapper.selectCount(new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, id).eq(FollowDO::getStatus,1));
        if(follows > 0){
            return Math.toIntExact(follows);
        }
        return 0;
    }
}
