package com.example.filter.aop;

import com.example.entity.dto.req.AddCommentReqDTO;
import com.example.entity.dto.req.TopicCreateReqDTO;
import com.example.entity.dto.req.TopicUpdateReqDTO;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import jakarta.annotation.Resource;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Aspect
@Component
public class TopicAspect {
    private static final Set<String> SENSITIVE_WORDS = Set.of(
        "黄色", "叼嘿", "做爱","操逼","sb","操你","看胸", // 色情相关
        "色情", "淫荡", "嫖娼", "性行为", "露点", "约炮", // 进一步的色情相关
        "蠢货", "傻瓜", "下贱", "垃圾", "痴呆", "变态", // 侮辱和攻击
        "种族歧视词", "性别歧视词", // 俩个类别为例
        "贩毒", "诈骗", "杀人", "强奸", // 违法相关
        "恐怖", "恐慌", "暴力",  "凶杀", // 其他敏感词
        "原神"
    );
    @Resource
    FlowUtils flowUtils;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Around("execution(* com.example.service.impl.TopicServiceImpl.createTopic(..))")
    public Object auditTopicContent(ProceedingJoinPoint joinPoint)throws Throwable{
        Object[] args = joinPoint.getArgs();
        TopicCreateReqDTO requestParam = (TopicCreateReqDTO) args[0]; // 获取请求参数
        Integer id = (Integer) args[1];
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + id;
        if (!flowUtils.limitPeriodCounterCheck(key, 3, 3600)){
            return "发文频繁,请稍后再试";
        }
        // 实现审核逻辑
        if (isContentValid(String.valueOf(requestParam.getContent())) || isContentValid(requestParam.getTitle())) {
            return "帖子内容包含敏感词，无法发布";
        }

        // 继续执行原始方法
        return joinPoint.proceed();
    }
    @Around("execution(* com.example.service.impl.TopicServiceImpl.updateTopic(..))")
    public Object auditTopicUpdateContent(ProceedingJoinPoint joinPoint)throws Throwable{
        Object[] args = joinPoint.getArgs();
        TopicUpdateReqDTO requestParam = (TopicUpdateReqDTO) args[0]; // 获取请求参数
        Integer id = (Integer) args[1];
        String s = stringRedisTemplate.opsForValue().get("xzn:topic:ban" + id);
        if (s != null && Integer.parseInt(s) >= 2) {
            return "修改频繁,请稍后再试";
        }
        // 实现审核逻辑
        if (isContentValid(String.valueOf(requestParam.getContent())) || isContentValid(requestParam.getTitle())) {
            if (!flowUtils.limitPeriodCounterCheck("xzn:topic:ban"+id, 2, 3600)){
                return "修改频繁,请稍后再试";
            }
            return "帖子内容包含敏感词，无法发布";
        }

        // 继续执行原始方法
        return joinPoint.proceed();
    }
    @Around("execution(* com.example.service.impl.TopicServiceImpl.addComment(..))")
    public Object auditCommentContent(ProceedingJoinPoint joinPoint)throws Throwable{
        Object[] args = joinPoint.getArgs();
        AddCommentReqDTO requestParam = (AddCommentReqDTO) args[1]; // 获取请求参数
        Integer id = (Integer) args[0];
        String s = stringRedisTemplate.opsForValue().get("xzn:comment:ban" + id);
        if (s != null && Integer.parseInt(s) >= 2) {
            return "修改频繁,请稍后再试";
        }
        // 实现审核逻辑
        if (isContentValid(String.valueOf(requestParam.getContent()))) {
            if (!flowUtils.limitPeriodCounterCheck("xzn:comment:ban"+id, 2, 1800)){
                return "评论频繁,请稍后再试";
            }
            return "评论内容包含敏感词，无法发布";
        }
        // 继续执行原始方法
        return joinPoint.proceed();
    }
    public boolean isContentValid(String content) {
        if(SensitiveWordHelper.contains(content)){
            return true;
        }
        return false;
    }
}
