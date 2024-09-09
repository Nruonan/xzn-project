package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dto.req.PrivacySaveReqDTO;
import com.example.entity.dto.resp.AccountPrivacyRespDTO;

/**
 * @author Nruonan
 * @description
 */
public interface AccountPrivacyService extends IService<AccountPrivacyDO> {

    void savePrivacy(int id, PrivacySaveReqDTO requestParam);

    AccountPrivacyRespDTO accountPrivacy(int id);
}
