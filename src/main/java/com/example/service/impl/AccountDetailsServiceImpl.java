package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dto.req.DetailsSaveReqDTO;
import com.example.entity.dto.resp.AccountDetailsRespDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.mapper.AccountDetailsMapper;
import com.example.service.AccountDetailsService;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper,AccountDetailsDO> implements AccountDetailsService {

    @Resource
    AccountService accountService;

    /**
     * 查询用户信息
     * @param id 用户id
     * @return 用户详细信息对象
     */
    @Override
    public AccountDetailsRespDTO findAccountDetailsById(int id) {

        AccountDetailsDO accountDetailsDO = baseMapper.selectById(id);
        return BeanUtil.toBean(accountDetailsDO, AccountDetailsRespDTO.class);
    }

    /**
     * 保存用户信息
     * @param requestParam 保存个人信息
     * @param id 用户id
     * @return 是否成功
     */

    @Override
    public boolean saveAccountDetails(int id, DetailsSaveReqDTO requestParam) {
        // 查询用户名
        AccountRespDTO accountRespDTO = accountService.findAccountByNameOrEmail(requestParam.getUsername());
        // 判断登录账号是否原账号
        if(accountRespDTO == null || accountRespDTO.getId() == id){
            // 更新Account数据库用户名
            LambdaUpdateWrapper<AccountDO> updateWrapper = Wrappers.lambdaUpdate(AccountDO.class)
                .eq(AccountDO::getId, id)
                .set(AccountDO::getUsername, requestParam.getUsername());
            accountService.update(updateWrapper);
            // 保存用户个人信息
            this.saveOrUpdate(AccountDetailsDO.builder()
                .id(id)
                .gender(requestParam.getGender())
                .phone(requestParam.getPhone())
                .wx(requestParam.getWx())
                .qq(requestParam.getQq())
                .desc(requestParam.getDesc()).build());
            return true;
        }
        return false;
    }
}
