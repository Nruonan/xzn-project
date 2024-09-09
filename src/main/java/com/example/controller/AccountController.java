package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.req.DetailsSaveReqDTO;
import com.example.entity.dto.req.ModifyEmailReqDTO;
import com.example.entity.dto.resp.AccountDetailsRespDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/user")
public class AccountController {

    @Resource
    AccountService accountService;

    @Resource
    AccountDetailsService accountDetailsService;

    @GetMapping("/info")
    public RestBean<AccountInfoRespDTO> findAccountById(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountInfoRespDTO dto = accountService.findAccountById(id);
        return RestBean.success(dto);
    }

    @GetMapping("/details")
    public RestBean<AccountDetailsRespDTO> findAccountDetailsById(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDetailsRespDTO dto = accountDetailsService.findAccountDetailsById(id);
        return RestBean.success(dto);
    }

    @PostMapping("/save-details")
    public RestBean<Void> saveAccountDetails(@RequestAttribute(Const.ATTR_USER_ID) int id, @RequestBody DetailsSaveReqDTO requestParam){
        boolean success = accountDetailsService.saveAccountDetails(id, requestParam);
        return success ? RestBean.success() :RestBean.failure(400,"此用户名已被其他用户使用，请重新更换！");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID) int id, @RequestBody @Valid ModifyEmailReqDTO requestParam){
        String success  = accountService.modifyEmail(id,requestParam);
        return success == null ? RestBean.success() :RestBean.failure(400,success);
    }
}
