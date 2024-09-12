package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.mapper.TopicTypeMapper;
import com.example.service.TopicService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class TopicServiceImpl implements TopicService {

    @Resource
    TopicTypeMapper topicTypeMapper;
    @Override
    public List<TopicTypeRespDTO> listTypes() {
        List<TopicTypeRespDTO> topicTypeRespDTOS = BeanUtil.copyToList(topicTypeMapper.selectList(null),
            TopicTypeRespDTO.class);
        return topicTypeRespDTOS;
    }
}
