package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.mapper.TopicMapper;
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
    TopicMapper topicMapper;
    @Override
    public List<TopicTypeRespDTO> listTypes() {
        List<TopicTypeRespDTO> topicTypeRespDTOS = BeanUtil.copyToList(topicMapper.selectList(null),
            TopicTypeRespDTO.class);
        return topicTypeRespDTOS;
    }
}
