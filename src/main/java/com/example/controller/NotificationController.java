package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.resp.NotificationRespDTO;
import com.example.service.NotificationService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {
    @Resource
    NotificationService service;

    @GetMapping("/list")
    public RestBean<List<NotificationRespDTO>> listNotification(@RequestAttribute(Const.ATTR_USER_ID)int id){
        return RestBean.success(service.findUserNotification(id));
    }

    @GetMapping("/delete")
    public RestBean<Void> deleteNotification(@RequestAttribute(Const.ATTR_USER_ID) @Min(0) int uid, @RequestParam @Min(0) int id){
        service.deleteUserNotification(id,uid);
        return RestBean.success();
    }

    @GetMapping("/delete-all")
    public RestBean<Void> deleteAllNotification(@RequestAttribute(Const.ATTR_USER_ID) @Min(0) int uid){
        service.deleteUserAllNotification(uid);
        return RestBean.success();
    }
}
