package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.FollowDO;
import com.example.entity.dto.resp.FansDetailRespDTO;
import java.util.List;


/**
 * @author Nruonan
 * @description
 */
public interface FollowService extends IService<FollowDO> {

    String followById(int id, int uid);

    boolean isFollow(int id, int uid);

    List<Integer> followList(int uid);


    Integer findFansById(int id);

    Integer findFollowsById(int id);

    List<FansDetailRespDTO> fansList(Integer userId);

    List<FansDetailRespDTO> followsList(Integer userId);

    List<FansDetailRespDTO> findTogether(Integer userId,Integer id);
}
