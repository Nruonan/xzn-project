package com.example.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RestBean;
import com.example.entity.dao.TopicTypeDO;
import com.example.entity.dto.req.TopicTypeReqDTO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.mapper.TopicTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author xzn
 * @create 2025/11/3
 * @desc 话题类型管理控制器
 */
@RestController
@RequestMapping("/api/admin/topic-type")
public class ForumTypeAdminController {

    @Resource
    private TopicTypeMapper topicTypeMapper;

    /**
     * 创建话题类型
     */
    @PostMapping("/create")
    public RestBean<Boolean> createTopicType(@RequestBody TopicTypeReqDTO reqDTO) {
        TopicTypeDO topicTypeDO = new TopicTypeDO();
        topicTypeDO.setName(reqDTO.getName());
        topicTypeDO.setDesc(reqDTO.getDesc());
        topicTypeDO.setColor(reqDTO.getColor());

        boolean result = topicTypeMapper.insert(topicTypeDO) > 0;
        return RestBean.success(result);
    }

    /**
     * 更新话题类型
     */
    @PostMapping("/update")
    public RestBean<Boolean> updateTopicType(@RequestBody TopicTypeReqDTO reqDTO) {
        TopicTypeDO topicTypeDO = new TopicTypeDO();
        topicTypeDO.setId(reqDTO.getId());
        topicTypeDO.setName(reqDTO.getName());
        topicTypeDO.setDesc(reqDTO.getDesc());
        topicTypeDO.setColor(reqDTO.getColor());

        boolean result = topicTypeMapper.updateById(topicTypeDO) > 0;
        return RestBean.success(result);
    }

    /**
     * 删除话题类型
     */
    @GetMapping("/delete")
    public RestBean<Boolean> deleteTopicType(@RequestParam("id") Integer id) {
        boolean result = topicTypeMapper.deleteById(id) > 0;
        return RestBean.success(result);
    }

    /**
     * 获取话题类型列表（分页）
     */
    @GetMapping("/list")
    public RestBean<Page<TopicTypeDO>> getTopicTypeList(
            @RequestParam(defaultValue = "1", value = "page") int pageNum,
            @RequestParam(defaultValue = "10", value = "size") int pageSize,
            @RequestParam(value = "name", required = false) String name) {

        Page<TopicTypeDO> page = new Page<>(pageNum, pageSize);
        QueryWrapper<TopicTypeDO> queryWrapper = new QueryWrapper<>();

        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }

        Page<TopicTypeDO> resultPage = topicTypeMapper.selectPage(page, queryWrapper);

        return RestBean.success(resultPage);
    }

    /**
     * 获取单个话题类型详情
     */
    @GetMapping("/detail")
    public RestBean<TopicTypeRespDTO> getTopicTypeDetail(@RequestParam("id") Integer id) {
        TopicTypeDO topicTypeDO = topicTypeMapper.selectById(id);
        if (topicTypeDO == null) {
            return RestBean.failure(404, "帖子类型不存在");
        }

        TopicTypeRespDTO respDTO = new TopicTypeRespDTO();
        BeanUtils.copyProperties(topicTypeDO, respDTO);
        return RestBean.success(respDTO);
    }
}