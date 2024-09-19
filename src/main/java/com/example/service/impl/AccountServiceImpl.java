package com.example.service.impl;


import static com.example.utils.Const.ASK_EMAIL_CODE;
import static com.example.utils.Const.VERIFY_EMAIL_DATA;
import static com.example.utils.Const.VERIFY_EMAIL_LIMIT;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dto.req.ChangePassWordReqDTO;
import com.example.entity.dto.req.ConfirmResetReqDTO;
import com.example.entity.dto.req.EmailRegisterReqDTO;
import com.example.entity.dto.req.EmailResetReqDTO;
import com.example.entity.dto.req.ModifyEmailReqDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.service.AccountService;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDO> implements AccountService {

    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    AccountPrivacyMapper privacyMapper;

    @Resource
    AccountDetailsMapper detailsMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PasswordEncoder passwordEncoder;
    @Resource
    RedissonClient redissonClient;
    /**
     * 从数据库中通过用户名或邮箱查找用户详细信息
     * @param username 用户名
     * @return 用户详细信息
     * @throws UsernameNotFoundException 如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountRespDTO account = this.findAccountByNameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
            .withUsername(username)
            .password(account.getPassword())
            .roles(account.getRole())
            .build();
    }

    @Override
    public String registerEmailVerifyCode(String type, String email, String ip) {
        RLock lock = redissonClient.getLock(ASK_EMAIL_CODE + ip);
        if(!lock.tryLock()){
            return "消息已发送，请稍后再试";
        }
        try{
            if (!this.verifyLimit(ip)) {
                return "请求频繁： 请稍后再试";
            }
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type",type,"email", email, "code", code,"retryCount",0);
            amqpTemplate.convertAndSend(Const.MQ_MAIL, data);
            stringRedisTemplate.opsForValue()
                .set(VERIFY_EMAIL_DATA + email, String.valueOf(code), 2, TimeUnit.MINUTES);
            return null;
        }catch (Exception e) {
            throw e;
        } finally {
           lock.unlock();
        }
    }

    @Override
    public String register(EmailRegisterReqDTO requestParam) {
        String email = requestParam.getEmail();
        String code = requestParam.getCode();
        if (code == null)return "请先获取验证码";
        if (!code.equals(getEmailVerifyCode(email)))return "验证码错误,请重新输入";
        if (this.existsAccountByEmail(email)) return "该邮件地址已被注册";
        String username = requestParam.getUsername();
        if(this.existsAccountByUsername(username)) return "该用户名已被他人使用，请重新更换";
        String password = passwordEncoder.encode(requestParam.getPassword());
        AccountDO accountDO = new AccountDO(null,requestParam.getUsername(),password,null,email,Const.ROLE_DEFAULT,new Date());
        if(!this.save(accountDO)){
            return "内部错误，注册失败";
        }else{
            this.deleteEmailVerifyCode(email);
            privacyMapper.insert(new AccountPrivacyDO(accountDO.getId()));
            AccountDetailsDO accountDetailsDO = new AccountDetailsDO();
            accountDetailsDO.setId(accountDO.getId());
            detailsMapper.insert(accountDetailsDO);
            return null;
        }
    }

    @Override
    public String resetConfirm(ConfirmResetReqDTO requestParam) {
        String email = requestParam.getEmail();
        String code = stringRedisTemplate.opsForValue().get(VERIFY_EMAIL_DATA + email);
        if(code == null) return "请先获取验证码";
        if (!code.equals(requestParam.getCode())) return "验证码错误，请重新输入";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetReqDTO requestParam) {
        String verify = this.resetConfirm(BeanUtil.toBean(requestParam, ConfirmResetReqDTO.class));
        if (verify != null)return verify;
        String email = requestParam.getEmail();
        String password = passwordEncoder.encode(requestParam.getPassword());
        LambdaUpdateWrapper<AccountDO> updateWrapper = Wrappers.lambdaUpdate(AccountDO.class).eq(AccountDO::getEmail, email)
            .set(AccountDO::getPassword, password);
        if(this.update(updateWrapper)){
            stringRedisTemplate.delete(VERIFY_EMAIL_DATA + email);

        }
        return null;
    }

    @Override
    public AccountInfoRespDTO findAccountById(int id) {
        LambdaQueryWrapper<AccountDO> queryWrapper = Wrappers.lambdaQuery(AccountDO.class).eq(AccountDO::getId, id);
        AccountDO accountDO = this.baseMapper.selectOne(queryWrapper);
        return BeanUtil.toBean(accountDO, AccountInfoRespDTO.class);
    }
    /**
     * 修改邮箱
     * @param id 用户名
     * @param requestParam 邮箱和验证码
     * @return 操作是否成功
     */
    @Override
    public String modifyEmail(int id, ModifyEmailReqDTO requestParam) {
        String email = requestParam.getEmail();
        String code = getEmailVerifyCode(requestParam.getEmail());
        if (code == null)return "请先获取验证码!";
        if (!code.equals(requestParam.getCode()))return "验证码错误，请重新输入";
        this.deleteEmailVerifyCode(email);
        AccountRespDTO account = this.findAccountByNameOrEmail(email);
        if (account != null && account.getId() != id){
            return "该电子邮件已被其他账号绑定，无法完成此操作";
        }
        LambdaUpdateWrapper<AccountDO> wrapper = Wrappers.lambdaUpdate(AccountDO.class)
            .set(AccountDO::getEmail, email)
            .eq(AccountDO::getId, id);
        update(wrapper);
        return null;
    }
    /**
     * 修改密码
     * @param id 用户名
     * @param requestParam 输入密码的表单
     * @return 操作是否成功
     */
    @Override
    public String changePassWord(int id, ChangePassWordReqDTO requestParam) {
        LambdaQueryWrapper<AccountDO> select = Wrappers.lambdaQuery(AccountDO.class).eq(AccountDO::getId, id);
        String password = baseMapper.selectOne(select).getPassword();
        if (!passwordEncoder.matches(requestParam.getPassword(),password))  return "原密码输入错误，请重新输入!";
        boolean success = this.update()
            .eq("id",id)
            .set("password",passwordEncoder.encode(requestParam.getNew_password()))
            .update();
        return success ? null :"未知错误，请联系管理员";
    }

    /**
     * 查询指定邮箱的用户是否已经存在
     * @param email 邮箱
     * @return 是否存在
     */
    private boolean existsAccountByEmail(String email){
        return this.baseMapper.exists(Wrappers.lambdaQuery(AccountDO.class).eq(AccountDO::getEmail, email));
    }


    /**
     * 查询指定用户名的用户是否已经存在
     * @param username 用户名
     * @return 是否存在
     */
    private boolean existsAccountByUsername(String username){
        return this.baseMapper.exists(Wrappers.lambdaQuery(AccountDO.class).eq(AccountDO::getUsername, username));
    }
    /**
     * 获取Redis中存储的邮件验证码
     * @param email 电邮
     * @return 验证码
     */
    private String getEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 移除Redis中存储的邮件验证码
     * @param email 电邮
     */
    private void deleteEmailVerifyCode(String email){
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

    private boolean verifyLimit(String ip){
        String key = VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key, 60);
    }

    @Override
    public AccountRespDTO findAccountByNameOrEmail(String text)  {
        // 根据名字查询用户
        LambdaQueryWrapper<AccountDO> queryWrapper = Wrappers.lambdaQuery(AccountDO.class)
            .eq(AccountDO::getUsername, text)
            .or()
            .eq(AccountDO::getEmail,text);
        AccountDO accountDO = baseMapper.selectOne(queryWrapper);
        AccountRespDTO accountRespDTO = new AccountRespDTO();
        if(accountDO == null) {
            return null;
        }
        else {
            accountRespDTO = BeanUtil.toBean(accountDO, AccountRespDTO.class);
        }
        return accountRespDTO;
    }
}
