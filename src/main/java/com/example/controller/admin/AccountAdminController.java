package com.example.controller.admin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.RestBean;
import com.example.entity.dao.AccountDO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.AccountRespDTO;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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
}
