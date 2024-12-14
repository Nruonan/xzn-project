package com.example.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.entity.RestBean;
import com.example.entity.dao.AccountDO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.filter.JwtAuthorizeFilter;
import com.example.filter.RequestLogFilter;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Nruonan
 * @description
 */

@Configuration
public class SecurityConfiguration {

    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    RequestLogFilter requestLogFilter;

    @Resource
    JwtUtils utils;

    @Resource
    FlowUtils flowUtils;

    @Resource
    AccountService service;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    /**
     * 针对于 SpringSecurity 6 的新版配置方法
     * @param http 配置器
     * @return 自动构建的内置过滤器链
     * @throws Exception 可能的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(conf -> conf
                .requestMatchers("/api/auth/**", "/error").permitAll()
                .requestMatchers("/images/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().hasAnyRole(Const.ROLE_DEFAULT,"admin")
            )
            .formLogin(conf -> conf
                .loginProcessingUrl("/api/auth/login")
                .failureHandler(this::handleProcess)
                .successHandler(this::handleProcess)
                .permitAll()
            )
            .logout(conf -> conf
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(this::onLogoutSuccess)
            )
            .exceptionHandling(conf -> conf
                .accessDeniedHandler(this::handleProcess)
                .authenticationEntryPoint(this::handleProcess)
            )
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(conf -> conf
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(requestLogFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthorizeFilter, RequestLogFilter.class)
            .build();
    }

    /**
     * 将多种类型的Handler整合到同一个方法中，包含：
     * - 登录成功
     * - 登录失败
     * - 未登录拦截/无权限拦截
     * @param request 请求
     * @param response 响应
     * @param exceptionOrAuthentication 异常或是验证实体
     * @throws IOException 可能的异常
     */
    private void handleProcess(HttpServletRequest request,
        HttpServletResponse response,
        Object exceptionOrAuthentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        String s = stringRedisTemplate.opsForValue().get("xzn:login:limit" + request.getRemoteAddr());
        if (s != null && Integer.parseInt(s)  >= 4){
            writer.write(RestBean
                .forbidden("登录频繁，请稍后重试").asJsonString());
            return;
        }
        if(exceptionOrAuthentication instanceof AccessDeniedException exception) {
            writer.write(RestBean
                .forbidden(exception.getMessage()).asJsonString());
        } else if (exceptionOrAuthentication instanceof BadCredentialsException exception) {
           flowUtils.limitPeriodCounterCheck("xzn:login:limit" + request.getRemoteAddr(), 5, 60);
                writer.write(RestBean
                    .forbidden(exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Exception exception) {
            writer.write(RestBean
                .unAuthorized(exception.getMessage()).asJsonString());
        } else if(exceptionOrAuthentication instanceof Authentication authentication){
            User user = (User) authentication.getPrincipal();
            AccountRespDTO account = service.findAccountByNameOrEmail(user.getUsername());
            String jwt = utils.createJwt(user, account.getUsername(), account.getId());
            String refreshJwt = utils.createRefreshJwt(user, account.getUsername(), account.getId());
            if(jwt == null && refreshJwt == null) {
                writer.write(RestBean.forbidden("登录验证频繁，请稍后再试").asJsonString());
            } else {
                AuthorizeRespDTO dto = BeanUtil.toBean(account, AuthorizeRespDTO.class);
                dto.setAccess_token(jwt);
                dto.setRefresh_token(refreshJwt);
                dto.setAccess_expire(utils.expireTime());
                writer.write(RestBean.success(dto).asJsonString());
            }
        }


    }

    /**
     * 退出登录处理，将对应的Jwt令牌列入黑名单不再使用
     * @param request 请求
     * @param response 响应
     * @param authentication 验证实体
     * @throws IOException 可能的异常
     */
    private void onLogoutSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");
        DecodedJWT decodedJWT = utils.resolveJwt(authorization);
        Integer id = utils.toId(decodedJWT);
        if(utils.invalidateJwt(authorization)) {
            String redisKey = String.format(Const.REFRESH_TOKEN_PREFIX, id);
            Boolean hasKey = this.stringRedisTemplate.hasKey(redisKey);
            if (hasKey) {
                // 删除原 token
                this.stringRedisTemplate.delete(redisKey);
            }

            writer.write(RestBean.success("退出登录成功").asJsonString());
            return;
        }
        writer.write(RestBean.failure(400, "退出登录失败").asJsonString());
    }
}
