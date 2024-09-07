package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.service.AccountService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
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

    @GetMapping("/info")
    public RestBean<AccountInfoRespDTO> findAccountById(@RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountInfoRespDTO dto = accountService.findAccountById(id);
        return RestBean.success(dto);
    }
}
