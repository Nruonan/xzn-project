package com.example.controller;

import com.example.config.user.UserContext;
import com.example.entity.RestBean;
import com.example.entity.dto.req.ChangePassWordReqDTO;
import com.example.entity.dto.req.DetailsSaveReqDTO;
import com.example.entity.dto.req.ModifyEmailReqDTO;
import com.example.entity.dto.req.PrivacySaveReqDTO;
import com.example.entity.dto.resp.AccountDetailsRespDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountPrivacyRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.entity.dto.resp.UserDetailsRespDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public RestBean<AccountInfoRespDTO> findAccountById(){

        AccountInfoRespDTO dto = accountService.findAccountById(UserContext.getUserId());
        return RestBean.success(dto);
    }

    @GetMapping("/details")
    public RestBean<AccountDetailsRespDTO> findAccountDetailsById(){
        Integer userId = UserContext.getUserId();
        AccountDetailsRespDTO dto = Optional
            .ofNullable(accountDetailsService.findAccountDetailsById(userId))
            .orElseGet(AccountDetailsRespDTO::new);
        return RestBean.success(dto);
    }

    @PostMapping("/save-details")
    public RestBean<Void> saveAccountDetails(@RequestBody DetailsSaveReqDTO requestParam){
        boolean success = accountDetailsService.saveAccountDetails(UserContext.getUserId(), requestParam);
        return success ? RestBean.success() :RestBean.failure(400,"此用户名已被其他用户使用，请重新更换！");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestBody @Valid ModifyEmailReqDTO requestParam){
        String success  = accountService.modifyEmail(UserContext.getUserId(),requestParam);
        return success == null ? RestBean.success() :RestBean.failure(400,success);
    }

    @PostMapping("/change-password")
    public RestBean<Void> changePassWord(@RequestBody @Valid ChangePassWordReqDTO requestParam){
        String success  = accountService.changePassWord(UserContext.getUserId(), requestParam);
        return success == null ? RestBean.success() :RestBean.failure(400,success);
    }

    @PostMapping("/save-privacy")
    public RestBean<Void> savePrivacy(@RequestBody @Valid
        PrivacySaveReqDTO requestParam){
        accountPrivacyService.savePrivacy(UserContext.getUserId(), requestParam);
        return RestBean.success();
    }

    @GetMapping("/privacy")
    public RestBean<AccountPrivacyRespDTO> privacy(){
        return RestBean.success(accountPrivacyService.accountPrivacy(UserContext.getUserId()));
    }
    @GetMapping("/detail")
    public RestBean<UserDetailsRespDTO> getDetailById(@RequestParam("id") int id,@RequestAttribute(Const.ATTR_USER_ID)int uid){
        return RestBean.success(accountService.getDetailById(id,uid));
    }
}
