package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.entity.RestBean;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.service.AccountService;
import com.example.service.AuthService;
import com.example.utils.FlowUtils;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.net.http.HttpRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class AuthServiceImpl implements AuthService {
    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    JwtUtils utils;
    @Resource
    AccountService accountService;
    @Resource
    FlowUtils flowUtils;
    /**
     * 用户登录方法
     * 
     * 该方法负责处理用户登录请求，包括验证用户提供的用户名和密码，
     * 生成JWT令牌和刷新令牌，并将用户信息及令牌信息封装到返回对象中
     * 
     * @param username 用户名或邮箱，用于用户身份验证
     * @param password 用户密码，用于用户身份验证
     * @return 登录成功后返回包含用户信息和令牌信息的AuthorizeRespDTO对象
     */
    @Override
    public AuthorizeRespDTO login(String username, String password, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        if (!flowUtils.limitPeriodCounterCheck("xzn:login:"+ remoteAddr, 3, 180)){
            throw new BadCredentialsException("登录频繁，请稍后再试!");
        }
        // 登录验证
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        // 加载用户详细信息
        UserDetails userDetails = accountService.loadUserByUsername(username);
        // 查询用户账户信息
        AccountRespDTO accountRespDTO = accountService.findAccountByNameOrEmail(username);
        // 创建JWT令牌
        String jwt = utils.createJwt(userDetails, username, accountRespDTO.getId());
        // 创建刷新令牌
        String refreshJwt = utils.createRefreshJwt(userDetails, username, accountRespDTO.getId());
        // 将用户信息转换并封装到返回对象中
        AuthorizeRespDTO dto = BeanUtil.toBean(accountRespDTO, AuthorizeRespDTO.class);
        // 设置访问令牌
        dto.setAccess_token(jwt);
        // 设置刷新令牌
        dto.setRefresh_token(refreshJwt);
        // 设置访问令牌过期时间
        dto.setAccess_expire(utils.expireTime());
        // 返回封装好的用户信息及令牌信息对象
        return dto;
    }
}
