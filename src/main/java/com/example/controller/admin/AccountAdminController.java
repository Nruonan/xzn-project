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
import jakarta.annotation.Resource;
import java.util.List;
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

    @PostMapping("/save")
    public RestBean<Void> saveAccount(@RequestBody JSONObject object){
        Integer id = object.getInteger("id");
        AccountInfoRespDTO account = service.findAccountById(id);
        AccountInfoRespDTO save = BeanUtil.copyProperties(object, AccountInfoRespDTO.class);
        BeanUtil.copyProperties(save,account, "password", "registerTime");
        AccountDO bean = BeanUtil.toBean(account, AccountDO.class);
        service.saveOrUpdate(bean);
        AccountDetailsRespDTO details = detailsService.findAccountDetailsById(id);
        AccountDetailsRespDTO saveDetails = object.getJSONObject("detail").toJavaObject(AccountDetailsRespDTO.class);
        BeanUtil.copyProperties(saveDetails, details);
        detailsService.saveOrUpdate(BeanUtil.toBean(details, AccountDetailsDO.class));
        AccountPrivacyRespDTO privacy = privacyService.accountPrivacy(id);
        AccountPrivacyRespDTO savePrivacy = object.getJSONObject("privacy").toJavaObject(AccountPrivacyRespDTO.class);
        BeanUtil.copyProperties(savePrivacy, privacy);
        AccountPrivacyDO bean1 = BeanUtil.toBean(privacy, AccountPrivacyDO.class);
        privacyService.saveOrUpdate(bean1);
        return RestBean.success();
    }
}
