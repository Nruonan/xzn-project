package com.example.config;

import com.example.entity.dto.resp.AccountRespDTO;
import com.example.service.AccountService;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
    @Resource
    PasswordEncoder passwordEncoder;
    @Resource
    AccountService accountService;


    /**
     * 认证用户凭据
     * 
     * 此方法负责接收一个Authentication对象，验证用户的身份和密码是否匹配
     * 如果验证成功，它将返回一个包含用户信息和权限的Authentication对象
     * 如果验证失败，它将抛出一个BadCredentialsException异常
     * 
     * @param authentication 包含用户信息的Authentication对象，包括用户名（principal）和密码（credentials）
     * @return 如果验证成功，返回一个包含用户信息和权限的新的UsernamePasswordAuthenticationToken对象
     * @throws AuthenticationException 如果验证过程中出现任何问题，抛出AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // 提取用户输入的用户名和密码
        String username = String.valueOf(authentication.getPrincipal());
        String password = String.valueOf(authentication.getCredentials());

        // 通过用户名获取用户详细信息
        UserDetails userDetails = accountService.loadUserByUsername(username);
        // 验证用户输入的密码与存储的密码是否匹配
        if (passwordEncoder.matches(password, userDetails.getPassword())){
            // 如果密码匹配，创建并返回一个新的认证对象，包含用户名、密码和用户权限
            return new UsernamePasswordAuthenticationToken(username, password, userDetails.getAuthorities());
        }
        // 如果密码不匹配，抛出异常
        throw new BadCredentialsException("密码错误，请重新登录!");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
