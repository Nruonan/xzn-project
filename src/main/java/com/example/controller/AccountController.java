package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.req.ChangePassWordReqDTO;
import com.example.entity.dto.req.DetailsSaveReqDTO;
import com.example.entity.dto.req.ModifyEmailReqDTO;
import com.example.entity.dto.req.PrivacySaveReqDTO;
import com.example.entity.dto.resp.AccountDetailsRespDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountPrivacyRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Optional;
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

    @Resource
    AccountPrivacyService accountPrivacyService;

    @GetMapping("/info")
    public RestBean<AccountInfoRespDTO> findAccountById(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountInfoRespDTO dto = accountService.findAccountById(id);
        return RestBean.success(dto);
    }

    @GetMapping("/details")
    public RestBean<AccountDetailsRespDTO> findAccountDetailsById(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountDetailsRespDTO dto = Optional
            .ofNullable(accountDetailsService.findAccountDetailsById(id))
            .orElseGet(AccountDetailsRespDTO::new);
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

    @PostMapping("/change-password")
    public RestBean<Void> changePassWord(@RequestAttribute(Const.ATTR_USER_ID) int id, @RequestBody @Valid ChangePassWordReqDTO requestParam){
        String success  = accountService.changePassWord(id, requestParam);
        return success == null ? RestBean.success() :RestBean.failure(400,success);
    }

    @PostMapping("/save-privacy")
    public RestBean<Void> savePrivacy(@RequestAttribute(Const.ATTR_USER_ID) int id,@RequestBody @Valid
        PrivacySaveReqDTO requestParam){
        accountPrivacyService.savePrivacy(id, requestParam);
        return RestBean.success();
    }

    @GetMapping("/privacy")
    public RestBean<AccountPrivacyRespDTO> privacy(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(accountPrivacyService.accountPrivacy(id));
    }
}
