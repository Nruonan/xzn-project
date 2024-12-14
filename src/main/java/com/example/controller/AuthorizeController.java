package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.req.ConfirmResetReqDTO;
import com.example.entity.dto.req.EmailRegisterReqDTO;
import com.example.entity.dto.req.EmailResetReqDTO;
import com.example.entity.dto.resp.AuthorizeRefreshRespDTO;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    @Resource
    AccountService accountService;

    @Resource
    ControllerUtils utils;
    @GetMapping("/refresh")
    public RestBean<AuthorizeRefreshRespDTO> refreshToken(@RequestParam("token") String token){
        return RestBean.success(accountService.refreshToken(token));
    }
    @GetMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                        HttpServletRequest request){
        return utils.messageHandle(() ->
            accountService.registerEmailVerifyCode(type, String.valueOf(email), request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Valid EmailRegisterReqDTO requestParam){
        return utils.messageHandle(() ->
            accountService.register(requestParam));
    }

    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody ConfirmResetReqDTO requestParam){
        return utils.messageHandle(() ->
            accountService.resetConfirm(requestParam));
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody EmailResetReqDTO requestParam){
        return utils.messageHandle(() ->
            accountService.resetEmailAccountPassword(requestParam));
    }
}
