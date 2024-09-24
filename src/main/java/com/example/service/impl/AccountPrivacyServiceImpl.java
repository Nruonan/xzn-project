package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dto.req.PrivacySaveReqDTO;
import com.example.entity.dto.resp.AccountPrivacyRespDTO;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountPrivacyService;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nruonan
 * @description
 */
@Service
public class AccountPrivacyServiceImpl extends ServiceImpl<AccountPrivacyMapper, AccountPrivacyDO> implements AccountPrivacyService {

    /**
     * @param id 用户id
     * @param requestParam 隐私属性（qq, wx,phone
     */
    @Override
    @Transactional
    public void savePrivacy(int id, PrivacySaveReqDTO requestParam) {
        AccountPrivacyDO privacy = Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacyDO(id));
        boolean status = requestParam.isStatus();
        switch (requestParam.getType()){
            case "phone" -> privacy.setPhone(status);
            case "qq" -> privacy.setQq(status);
            case "wx" -> privacy.setWx(status);
            case "gender" -> privacy.setGender(status);
            case "email" -> privacy.setEmail(status);
            case "remind" -> privacy.setRemind(status);
        }
        this.saveOrUpdate(privacy);
    }
    @Override
    public AccountPrivacyRespDTO accountPrivacy(int id){
        AccountPrivacyDO accountPrivacyDO = Optional.ofNullable(this. getById(id)).orElse(new AccountPrivacyDO(id));
        return BeanUtil.toBean(accountPrivacyDO,AccountPrivacyRespDTO.class);
    }
}
