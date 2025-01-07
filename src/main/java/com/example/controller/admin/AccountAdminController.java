package com.example.controller.admin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.RestBean;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dto.resp.AccountDetailsRespDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountPrivacyRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.service.AccountDetailsService;
import com.example.service.AccountPrivacyService;
import com.example.service.AccountService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/admin/user")
public class AccountAdminController {

    @Resource
    AccountService service;
    @Resource
    AccountDetailsService detailsService;

    @Resource
    AccountPrivacyService privacyService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Value("${spring.security.jwt.expire}")
    private int expire;
    /**
     * 获取账户列表信息
     * 该方法通过GET请求处理账户列表的请求，根据指定的页码和页面大小进行分页查询
     * 
     * @param page 页码，从0开始
     * @param size 页面大小，表示每页返回的记录数
     * @return 返回包含账户列表和总记录数的RestBean对象
     */
    @GetMapping("/list")
    public RestBean<JSONObject> accountList(int page, int size){
        // 创建一个空的JSONObject对象，用于存储查询结果
        JSONObject object = new JSONObject();
    
        // 调用service的page方法进行分页查询，并将查询结果转换为AccountRespDTO对象列表
        List<AccountInfoRespDTO> list = service.page(Page.of(page, size))
            .getRecords()
            .stream()
            .map(a -> BeanUtil.toBean(a, AccountInfoRespDTO.class))
            .toList();
    
        // 将总记录数放入JSONObject中
        object.put("total", service.count());
        // 将转换后的账户列表放入JSONObject中
        object.put("list", list);
    
        // 返回包含查询结果的RestBean对象，表示操作成功
        return RestBean.success(object);
    }

    @GetMapping("/detail")
    public RestBean<JSONObject> accountDetail(int id){
        JSONObject object = new JSONObject();
        object.put("detail", detailsService.findAccountDetailsById(id) );
        object.put("privacy", privacyService.accountPrivacy(id));
        return RestBean.success(object);
    }

    /**
     * 保存账户信息
     * 此方法处理账户信息的保存请求，包括账户基本信息、详细信息和隐私设置
     * 它首先根据ID获取现有账户信息，然后将请求体中的数据与现有数据合并，
     * 处理账户禁用状态，更新或保存账户信息、详细信息和隐私设置
     *
     * @param object 包含要保存的账户信息的JSON对象，包括账户基本信息、详细信息和隐私设置
     * @return 返回表示操作成功的RestBean对象，不包含具体数据
     */
    @PostMapping("/save")
    public RestBean<Void> saveAccount(@RequestBody JSONObject object){
        // 获取账户ID
        Integer id = object.getInteger("id");
        // 根据ID查找现有账户信息
        AccountInfoRespDTO account = service.findAccountById(id);
        // 将请求体中的账户信息复制到一个新的AccountInfoRespDTO对象中
        AccountInfoRespDTO save = BeanUtil.copyProperties(object, AccountInfoRespDTO.class);
        // 处理账户禁用状态
        handleBanned(account, save);
        // 将新账户信息中的密码和注册时间复制回现有账户信息
        BeanUtil.copyProperties(save,account, "password", "registerTime");
        // 将现有账户信息转换为AccountDO对象
        AccountDO bean = BeanUtil.toBean(account, AccountDO.class);
        // 保存或更新账户信息
        service.saveOrUpdate(bean);
        
        // 获取账户详细信息
        AccountDetailsRespDTO details = detailsService.findAccountDetailsById(id);
        // 将请求体中的账户详细信息复制到一个新的AccountDetailsRespDTO对象中
        AccountDetailsRespDTO saveDetails = object.getJSONObject("detail").toJavaObject(AccountDetailsRespDTO.class);
        // 将新账户详细信息复制回现有详细信息
        BeanUtil.copyProperties(saveDetails, details);
        // 保存或更新账户详细信息
        detailsService.saveOrUpdate(BeanUtil.toBean(details, AccountDetailsDO.class));
        
        // 获取账户隐私设置
        AccountPrivacyRespDTO privacy = privacyService.accountPrivacy(id);
        // 将请求体中的账户隐私设置复制到一个新的AccountPrivacyRespDTO对象中
        AccountPrivacyRespDTO savePrivacy = object.getJSONObject("privacy").toJavaObject(AccountPrivacyRespDTO.class);
        // 将新账户隐私设置复制回现有隐私设置
        BeanUtil.copyProperties(savePrivacy, privacy);
        // 将现有账户隐私设置转换为AccountPrivacyDO对象
        AccountPrivacyDO bean1 = BeanUtil.toBean(privacy, AccountPrivacyDO.class);
        // 保存或更新账户隐私设置
        privacyService.saveOrUpdate(bean1);
        
        // 返回操作成功响应
        return RestBean.success();
    }

    /**
     * 处理用户封禁状态的变化
     * 当用户从非封禁状态变为封禁状态时，记录封禁信息到Redis，并设置过期时间
     * 当用户从封禁状态变为非封禁状态时，从Redis中删除封禁信息
     * 
     * @param old  用户的原始账户信息，用于判断用户之前的封禁状态
     * @param current  用户的当前账户信息，用于判断用户当前的封禁状态
     */
    private void handleBanned(AccountInfoRespDTO old, AccountInfoRespDTO current){
        // 构造Redis键，用于存储或删除封禁信息
        String key = Const.BANNED_BLOCK + old.getId();
        
        // 如果用户从非封禁状态变为封禁状态
        if (!old.isBanned() && current.isBanned()){
            // 在Redis中设置封禁信息，有效期为指定小时数
            stringRedisTemplate.opsForValue().set(key, "true", expire, TimeUnit.HOURS);
        // 如果用户从封禁状态变为非封禁状态
        }else if(old.isBanned() && !current.isBanned()){
            // 从Redis中删除封禁信息
            stringRedisTemplate.delete(key);
        }
    }
}
