package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.entity.RestBean;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.service.AccountService;
import com.example.service.AuthService;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.security.authentication.AuthenticationManager;
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
    @Override
    public AuthorizeRespDTO login(String username, String password) {
        // 登录验证
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        UserDetails userDetails = accountService.loadUserByUsername(username);
        AccountRespDTO accountRespDTO = accountService.findAccountByNameOrEmail(username);
        String jwt = utils.createJwt(userDetails, username, accountRespDTO.getId());
        String refreshJwt = utils.createRefreshJwt(userDetails, username, accountRespDTO.getId());
        AuthorizeRespDTO dto = BeanUtil.toBean(accountRespDTO, AuthorizeRespDTO.class);
        dto.setAccess_token(jwt);
        dto.setRefresh_token(refreshJwt);
        dto.setAccess_expire(utils.expireTime());
        return dto;
    }
}
