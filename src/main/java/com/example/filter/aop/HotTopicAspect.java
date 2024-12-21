package com.example.filter.aop;

import com.example.entity.dao.Interact;
import com.example.entity.dto.req.AddCommentReqDTO;
import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
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
public class HotTopicAspect {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Around("execution(* com.example.service.impl.TopicServiceImpl.addComment(..))")
    public Object addCommentAfter(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        AddCommentReqDTO requestParam = (AddCommentReqDTO) args[1]; // 获取请求参数
        // 执行目标方法并获取返回结果
        Object result = joinPoint.proceed();
        if (result == null){
            stringRedisTemplate.opsForSet().add("xzn:hot:topic", String.valueOf(requestParam.getTid()));
            stringRedisTemplate.expire("xzn:hot:topic", 3, TimeUnit.HOURS);
            stringRedisTemplate.opsForHash().increment("xzn:topic:hot:" + requestParam.getTid() , "comment", 1);
            stringRedisTemplate.expire("xzn:topic:hot:" + requestParam.getTid(), 3, TimeUnit.HOURS);
        }
        return result;
    }

    @Around("execution(* com.example.service.impl.TopicServiceImpl.interact(..))")
    public Object addInteract(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        boolean state = (boolean) args[1];
        Interact interact = (Interact) args[0];
        stringRedisTemplate.opsForSet().add("xzn:hot:topic", String.valueOf(interact.getTid()));
        stringRedisTemplate.expire("xzn:hot:topic", 3, TimeUnit.HOURS);
        if(state){
            if("like".equals(interact.getType())) {
                stringRedisTemplate.opsForHash().increment("xzn:topic:hot:" + interact.getTid(), "like", 1);
                stringRedisTemplate.expire("xzn:topic:hot:" + interact.getTid(), 3, TimeUnit.HOURS);
            }else{
                stringRedisTemplate.opsForHash().increment("xzn:topic:hot:" + interact.getTid(), "collect", 1);
                stringRedisTemplate.expire("xzn:topic:hot:" + interact.getTid(), 3, TimeUnit.HOURS);
            }
        }else{
            if("like".equals(interact.getType())) {
                stringRedisTemplate.opsForHash().increment("xzn:topic:hot:" + interact.getTid(), "like", -1);

            }else{
                stringRedisTemplate.opsForHash().increment("xzn:topic:hot:" + interact.getTid(), "collect", -1);
            }
        }

        return joinPoint.proceed();
    }
}
