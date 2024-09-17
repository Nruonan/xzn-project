package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.Interact;
import com.example.entity.dao.TopicDO;
import com.example.entity.dao.TopicTypeDO;
import com.example.entity.dto.req.TopicCreateReqDTO;
import com.example.entity.dto.resp.TopTopicRespDTO;
import com.example.entity.dto.resp.TopicCollectRespDTO;
import com.example.entity.dto.resp.TopicDetailRespDTO;
import com.example.entity.dto.resp.TopicPreviewRespDTO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import java.util.List;

/**
 * @author Nruonan
 * @description
 */

public interface TopicService  extends IService<TopicDO> {
    List<TopicTypeRespDTO> listTypes();

    String createTopic(TopicCreateReqDTO requestParam, int id);

    List<TopicPreviewRespDTO> listTopicByPage(int page, int type);

    TopicDetailRespDTO getTopic(int id);


    List<TopTopicRespDTO> listTopTopics();

    void interact(Interact interact, boolean state);

    List<TopicCollectRespDTO> getCollects(int id);
}
