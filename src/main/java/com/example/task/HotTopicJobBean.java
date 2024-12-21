package com.example.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.entity.dao.TopicDO;
import com.example.mapper.TopicMapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Nruonan
 * @description
 */
@Slf4j
public class HotTopicJobBean extends QuartzJobBean {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    TopicMapper topicMapper;
    // 权重配置
    private static final double COMMENT_WEIGHT = 1.0;
    private static final double HOT_WEIGHT = 0.8;
    private static final double COLLECT_WEIGHT = 1.2;

    // 时间衰减配置
    private static final double GRAVITY = 1.8;    private static final long TIME_UNIT_HOURS = 12; // 每12小时进行一次衰减
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        List<Integer> tidList = Objects.requireNonNull(stringRedisTemplate.opsForSet().members("xzn:hot:topic")).stream()
            .map(Integer::parseInt)
            .toList();
        List<TopicDO> topicDOS = topicMapper.selectList(
            new LambdaQueryWrapper<>(TopicDO.class).in(TopicDO::getId, tidList));

        HashMap<Integer, Double> map = refreshHotTopics(topicDOS);
        System.out.println("ceshi");
        for(Map.Entry<Integer, Double> entry : map.entrySet()){
            topicMapper.update(new LambdaUpdateWrapper<>(TopicDO.class)
                .eq(TopicDO::getId, entry.getKey())
                .set(TopicDO::getScore, entry.getValue()));
        }
    }
    public HashMap<Integer, Double> refreshHotTopics(List<TopicDO> topicList) {
        HashMap<Integer, Double> map = new HashMap<>();
        for(TopicDO topic : topicList){
            Map<Object, Object> hotTopic = stringRedisTemplate.opsForHash().entries("xzn:topic:hot:" + topic.getId());
            if(hotTopic.isEmpty())continue;
            // 获取字段值并进行空值检查
            int comment = (hotTopic.get("comment") != null) ? Integer.parseInt(hotTopic.get("comment").toString()) : 0;
            int like = (hotTopic.get("like") != null) ? Integer.parseInt(hotTopic.get("like").toString()) : 0;
            int collect = (hotTopic.get("collect") != null) ? Integer.parseInt(hotTopic.get("collect").toString()) : 0;

            LocalDateTime now = LocalDateTime.now();

            // 计算帖子年龄（小时）
            long hoursAge = ChronoUnit.HOURS.between(topic.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime(), now);
            // 计算基础分数（考虑各项指标的权重）
            double baseScore = (comment * COMMENT_WEIGHT) +
                (like * HOT_WEIGHT) +
                (collect * COLLECT_WEIGHT);

            // 计算时间衰减
            double timeDecay = Math.pow(hoursAge / 12.0, GRAVITY);
            timeDecay = Math.max(timeDecay, 0.1); // 避免除以零

            // 计算最终热度分数
            double hotnessScore = baseScore / timeDecay;
            map.put(topic.getId(),  Math.round(hotnessScore * 10000.0) / 10000.0);
        };
        return map;
    }
}
