package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.AccountDO;
import com.example.entity.dto.req.ChangePassWordReqDTO;
import com.example.entity.dto.req.ConfirmResetReqDTO;
import com.example.entity.dto.req.EmailRegisterReqDTO;
import com.example.entity.dto.req.EmailResetReqDTO;
import com.example.entity.dto.req.ModifyEmailReqDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.entity.dto.resp.AuthorizeRefreshRespDTO;
import com.example.entity.dto.resp.AuthorizeRespDTO;
import com.example.entity.dto.resp.UserDetailsRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Nruonan
 * @description
 */
public interface AccountService extends IService<AccountDO> , UserDetailsService {
    AccountRespDTO findAccountByNameOrEmail(String text);
    String registerEmailVerifyCode(String type, String email, String ip);

    String register(EmailRegisterReqDTO requestParam);
    String resetConfirm(ConfirmResetReqDTO resetReqDTO);

    String resetEmailAccountPassword(EmailResetReqDTO requestParam);

    AccountInfoRespDTO findAccountById(int id);

    String modifyEmail(int id, ModifyEmailReqDTO requestParam);

    String changePassWord(int id, ChangePassWordReqDTO requestParam);

    UserDetailsRespDTO getDetailById(int id,int uid);

    AuthorizeRefreshRespDTO refreshToken(String token);
}
