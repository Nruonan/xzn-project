package com.example.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RestBean;
import com.example.entity.dao.TopicDO;
import com.example.entity.dto.req.TopicUpdateReqDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.TopicInfoRespDTO;
import com.example.service.AccountService;
import com.example.service.TopicService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.redis.listener.Topic;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/admin/forum")
public class ForumAdminController {


    @Resource
    TopicService service;

    @GetMapping("/list")
    public RestBean<JSONObject> topicList(int page, int size){
        // 创建一个空的JSONObject对象，用于存储查询结果
        JSONObject object = new JSONObject();

        // 调用service的page方法进行分页查询，并将查询结果转换为TopicInfoRespDTO对象列表
        List<TopicInfoRespDTO> list = service.page(Page.of(page, size))
            .getRecords()
            .stream()
            .map(a -> BeanUtil.toBean(a, TopicInfoRespDTO.class))
            .toList();

        // 将总记录数放入JSONObject中
        object.put("total", service.count());
        // 将转换后的账户列表放入JSONObject中
        object.put("list", list);

        // 返回包含查询结果的RestBean对象，表示操作成功
        return RestBean.success(object);
    }

    @GetMapping("/detail")
    public RestBean<TopicInfoRespDTO> topicDetail(@RequestParam("id") int id){
        return RestBean.success(BeanUtil.toBean(service.getById(id), TopicInfoRespDTO.class));
    }

    @PostMapping("/update")
    public RestBean<Void> update(@RequestBody TopicDO topicDO){
        service.updateById(topicDO);
        return RestBean.success();
    }

    @GetMapping("/remove")
    public RestBean<Void> removeById(@RequestParam("id") int id){
        service.removeById(id);
        return RestBean.success();
    }
}
