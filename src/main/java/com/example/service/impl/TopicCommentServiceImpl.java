package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.TopicCommentDO;
import com.example.mapper.TopicCommentMapper;
import com.example.service.TopicCommentService;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class TopicCommentServiceImpl extends ServiceImpl<TopicCommentMapper, TopicCommentDO> implements TopicCommentService {

}
