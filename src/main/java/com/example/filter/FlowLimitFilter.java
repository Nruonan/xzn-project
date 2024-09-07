package com.example.filter;

import cn.hutool.log.Log;
import com.example.entity.RestBean;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component
@Slf4j
@Order(Const.ORDER_LIMIT)
public class FlowLimitFilter extends HttpFilter {

    @Resource
    StringRedisTemplate stringRedisTemplate;
    //指定时间内最大请求次数限制
    @Value("${spring.web.flow.limit}")
    int limit;
    //计数时间周期
    @Value("${spring.web.flow.period}")
    int period;
    //超出请求限制封禁时间
    @Value("${spring.web.flow.block}")
    int block;

    @Resource
    FlowUtils utils;


    @Resource
    RedissonClient redissonClient;
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String address = request.getRemoteAddr();
        if (!tryCount(address)) {
            this.writeBlockMessage(response);
        } else {
            chain.doFilter(request, response);
        }
    }
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.write(RestBean.forbidden("操作频繁，请稍后再试").asJsonString());
    }
    private boolean tryCount(String ip) {
        String counterKey = Const.FLOW_LIMIT_COUNTER + ip;

        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.FLOW_LIMIT_BLOCK + ip)))
            return false;
        // 判断键是否存在，如果不存在，初始化并设置过期时间
        Boolean isNewKey = stringRedisTemplate.opsForValue().setIfAbsent(counterKey, "1", period, TimeUnit.SECONDS);

        // 如果键是新创建的，初始值为 1 并设置了过期时间
        if (isNewKey != null && isNewKey) {
            return true;
        }

        // 如果键已经存在，递增计数
        Long requestCount = stringRedisTemplate.opsForValue().increment(counterKey);

        // 检查请求次数是否超限
        if (requestCount != null && requestCount < limit) {
            return true;  // 请求次数未超限
        }

        RLock lock = redissonClient.getLock(ip.intern());
        try {
            // 尝试在指定时间内获取锁，比如 2 秒
            if (!lock.tryLock()) {
                log.warn("无法获取锁，IP: {}，可能操作正在进行中", ip);
                return false;
            }
            String blockKey = Const.FLOW_LIMIT_BLOCK + ip;
            if(requestCount != null && requestCount >= limit){
                stringRedisTemplate.opsForValue().set(blockKey, "", block, TimeUnit.SECONDS);
                stringRedisTemplate.delete(counterKey);
            }
            return true;

        }  finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
