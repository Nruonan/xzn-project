package com.example.config;

import com.example.mapper.TopicMapper;
import com.example.task.HotTopicJobBean;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Nruonan
 * @description
 */
@Configuration
@Slf4j
public class QuartzConfiguration {

    /**
     * 创建并配置一个JobDetail实例
     * @return JobDetail 实例，描述了监控任务的作业
     */
    @Bean
    public JobDetail jobDetailFactoryBean() {
        // 使用JobBuilder构建一个MonitorJobBean类的作业，并为其指定唯一的标识"monitor-task"
        // storeDurably()方法确保即使没有触发器与之关联，作业实例也会被持久化
        return JobBuilder.newJob(HotTopicJobBean.class)
            .withIdentity("hot-topic")
            .storeDurably()
            .build();
    }

    /**
     * 该触发器用于按照Cron表达式定义的时间间隔触发指定的Job任务
     * @param detail JobDetail对象，定义了任务的详细信息，包括任务类、任务名称等
     * @return 返回一个Trigger对象，用于触发定义在JobDetail中的任务
     */
    @Bean
    public Trigger cronTriggerFactoryBean(JobDetail detail) {
        // 构建一个新的Trigger，用于触发任务
        return TriggerBuilder.newTrigger()
            // 指定该Trigger触发的Job
            .forJob(detail)
            // 设置Trigger的标识为"hot-topic"
            .withIdentity("hot-topic")
            // 使用Cron表达式定义触发时间间隔，这里每3个小时触发一次
//        0 0 */3 * * ? */10 * * * * ?
            .withSchedule(org.quartz.CronScheduleBuilder.cronSchedule("0 0 */3 * * ?"))
            // 构建并返回Trigger对象
            .build();
    }
}
