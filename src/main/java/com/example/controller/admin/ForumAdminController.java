package com.example.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RestBean;
import com.example.entity.dao.TopicCommentDO;
import com.example.entity.dao.TopicDO;
import com.example.entity.dto.req.TopicUpdateReqDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.TopicInfoRespDTO;
import com.example.service.AccountService;
import com.example.service.TopicCommentService;
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

    @Resource
    TopicCommentService commentService;

    @GetMapping("/list")
    public RestBean<JSONObject> topicList(@RequestParam("page") int page, @RequestParam("size") int size,
                                          @RequestParam(value = "searchTitle",required = false) String searchTitle,
                                          @RequestParam(value = "typeId", required = false) Integer typeId){
        // 创建一个空的JSONObject对象，用于存储查询结果
        JSONObject object = new JSONObject();

        Page<TopicDO> pageResult;
        // 根据是否有搜索条件执行不同的分页查询
        if (searchTitle != null && !searchTitle.isEmpty() && typeId != null) {
            // 创建查询条件包装器
            LambdaQueryWrapper<TopicDO> queryWrapper = new LambdaQueryWrapper<>();
            // 添加模糊查询条件，这里假设按评论内容进行搜索
            queryWrapper.eq(TopicDO::getStatus, 1);
            queryWrapper.eq(TopicDO::getType, typeId);
            queryWrapper.like(TopicDO::getTitle, searchTitle);
            // 执行带条件的分页查询
            pageResult = service.page(Page.of(page, size), queryWrapper);
        } else if (searchTitle != null && !searchTitle.isEmpty()) {
            // 创建查询条件包装器
            LambdaQueryWrapper<TopicDO> queryWrapper = new LambdaQueryWrapper<>();
            // 添加模糊查询条件，这里假设按评论内容进行搜索
            queryWrapper.eq(TopicDO::getStatus, 1);
            queryWrapper.like(TopicDO::getTitle, searchTitle);
            // 执行带条件的分页查询
            pageResult = service.page(Page.of(page, size), queryWrapper);

        } else if (typeId != null) {
            // 创建查询条件包装器
            LambdaQueryWrapper<TopicDO> queryWrapper = new LambdaQueryWrapper<>();
            // 添加查询条件，这里假设按评论内容进行搜索
            queryWrapper.eq(TopicDO::getType, typeId);
            queryWrapper.eq(TopicDO::getStatus, 1);
            // 执行带条件的分页查询
            pageResult = service.page(Page.of(page, size), queryWrapper);
        } else {
            // 创建查询条件包装器
            LambdaQueryWrapper<TopicDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TopicDO::getStatus, 1);
            // 执行带条件的分页查询
            pageResult = service.page(Page.of(page, size), queryWrapper);
        }

        // 将查询结果转换为TopicCommentDO对象列表
        List<TopicInfoRespDTO> list = pageResult.getRecords()
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

    @GetMapping("/comment/list")
    public RestBean<JSONObject> topicCommentList(int page, int size, String searchTitle){
        // 创建一个空的JSONObject对象，用于存储查询结果
        JSONObject object = new JSONObject();

        Page<TopicCommentDO> pageResult;
        // 根据是否有搜索条件执行不同的分页查询
        if (searchTitle != null && !searchTitle.isEmpty()) {
            // 创建查询条件包装器
            LambdaQueryWrapper<TopicCommentDO> queryWrapper = new LambdaQueryWrapper<>();
            // 添加模糊查询条件，这里假设按评论内容进行搜索
            queryWrapper.like(TopicCommentDO::getContent, searchTitle);
            // 执行带条件的分页查询
            pageResult = commentService.page(Page.of(page, size), queryWrapper);
        } else {
            // 执行普通分页查询
            pageResult = commentService.page(Page.of(page, size));
        }

        // 将查询结果转换为TopicCommentDO对象列表
        List<TopicCommentDO> list = pageResult.getRecords()
            .stream()
            .map(a -> BeanUtil.toBean(a, TopicCommentDO.class))
            .toList();

        // 将总记录数放入JSONObject中
        object.put("total", commentService.count());
        // 将转换后的账户列表放入JSONObject中
        object.put("list", list);

        // 返回包含查询结果的RestBean对象，表示操作成功
        return RestBean.success(object);
    }

    @GetMapping("/comment/detail")
    public RestBean<TopicCommentDO> topicCommentDetail(@RequestParam("id") int id){
        return RestBean.success(BeanUtil.toBean(commentService.getById(id), TopicCommentDO.class));
    }

    @PostMapping("/comment/update")
    public RestBean<Void> updateComment(@RequestBody TopicCommentDO topicDO){
        commentService.updateById(topicDO);
        return RestBean.success();
    }

    @GetMapping("/comment/remove")
    public RestBean<Void> removeCommentById(@RequestParam("id") int id){
        commentService.removeById(id);
        return RestBean.success();
    }


}
