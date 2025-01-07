package com.example.filter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.config.user.UserContext;
import com.example.config.user.UserInfoDTO;
import com.example.entity.RestBean;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author Nruonan
 * @description
 */
@Component
@Order(-100)
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头
        String authorization = request.getHeader("Authorization");
        // 从请求头解析jwt
        DecodedJWT jwt = utils.resolveJwt(authorization);
        if(jwt != null) {
            //开始解析成UserDetails对象，如果得到的是null说明解析失败，JWT有问题
            UserDetails user = utils.toUser(jwt);
            // 判断是否被封禁
            if (!stringRedisTemplate.hasKey(Const.BANNED_BLOCK + utils.toId(jwt))){
                //验证没有问题，那么就可以开始创建Authentication了，这里我们跟默认情况保持一致
                //使用UsernamePasswordAuthenticationToken作为实体，填写相关用户信息进去
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                //然后直接把配置好的Authentication塞给SecurityContext表示已经完成验证
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // 将该用户jwt添加到请求头
                request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
                UserContext.setUser(new UserInfoDTO(utils.toId(jwt)));
            }else{
                utils.invalidateJwt(authorization);
            }

        }
        try{
            // 触发http请求
            filterChain.doFilter(request, response);
        }finally {
            UserContext.removeUser();
        }
    }
}
