package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dto.req.DetailsSaveReqDTO;
import com.example.entity.dto.resp.AccountDetailsRespDTO;

/**
 * @author Nruonan
 * @description
 */
public interface AccountDetailsService extends IService<AccountDetailsDO>  {

    AccountDetailsRespDTO findAccountDetailsById(int id);

    boolean saveAccountDetails(int id, DetailsSaveReqDTO requestParam);
}
