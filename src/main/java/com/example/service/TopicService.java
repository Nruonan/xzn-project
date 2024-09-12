package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.TopicTypeDO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import java.util.List;

/**
 * @author Nruonan
 * @description
 */

public interface TopicService  {
    List<TopicTypeRespDTO> listTypes();
}
