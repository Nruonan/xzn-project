package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.service.AccountService;
import com.example.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Resource
    private AuthService authService;

    @PostMapping("/login")
    public RestBean<AuthorizeRespDTO> login(@RequestParam String username, @RequestParam String password){
        return RestBean.success(authService.login(username,password));
    }
}
