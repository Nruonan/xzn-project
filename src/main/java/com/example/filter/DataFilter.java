package com.example.filter;


import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component
@Order(-99)
public class DataFilter extends HttpFilter {


    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    JwtUtils utils;
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        String remoteAddr = request.getRemoteAddr();
        String date = df.format(new Date());
        // 获取请求头
        Integer userId = (Integer)request.getAttribute(Const.ATTR_USER_ID);
        if (userId == null){
            chain.doFilter(request, response);
            return;
        }

        recordUV(date,remoteAddr);
        recordDau(date, userId);
        chain.doFilter(request, response);
    }

    /**
     * 将指定的 IP 计入当天的 UV
     * @param ip
     */
    private void recordUV(String date,String ip) {
        stringRedisTemplate.opsForHyperLogLog().add("xzn:uv:" + date, ip);
    }

    /**
     * 将指定的 userID 计入当天的 UV
     * @param ip
     */
    private  void recordDau(String date,int userId) {
        stringRedisTemplate.opsForHyperLogLog().add("xzn:dau:" + date, String.valueOf(userId));
    }
}
