package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.NotificationDO;
import com.example.entity.dto.resp.NotificationRespDTO;
import java.util.List;


/**
 * @author Nruonan
 * @description
 */
public interface NotificationService extends IService<NotificationDO> {
    List<NotificationRespDTO> findUserNotification(int uid);
    void deleteUserNotification(int id, int uid);
    void deleteUserAllNotification(int uid);
    void addNotification(int uid, String title, String content,String type, String url);
}
