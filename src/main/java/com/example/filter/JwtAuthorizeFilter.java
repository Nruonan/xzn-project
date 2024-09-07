package com.example.filter;

import cn.hutool.json.JSONObject;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.Const;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    @Resource
    JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        // 获取请求头
        String authorization = request.getHeader("Authorization");
        // 从请求头解析jwt
        DecodedJWT jwt = utils.resolveJwt(authorization);
        if(jwt != null) {
            // 得到对象
            UserDetails user = utils.toUser(jwt);
            // 创建用户权限对象
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            // 存入详细资料
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 设置通过验证
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 将该用户jwt添加到请求重
            request.setAttribute(Const.ATTR_USER_ID, utils.toId(jwt));
        }
        // 触发http请求
        filterChain.doFilter(request, response);
    }
}
