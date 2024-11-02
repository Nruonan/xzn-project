package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.FollowDO;
import java.util.List;


/**
 * @author Nruonan
 * @description
 */
public interface FollowService extends IService<FollowDO> {

    String followById(int id, boolean type, int uid);

    boolean isFollow(int id, int uid);

    List<Integer> followList(int uid);
}
