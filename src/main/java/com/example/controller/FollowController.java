package com.example.controller;


import com.example.entity.RestBean;
import com.example.service.FollowService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
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
@RequestMapping("/follow")
public class FollowController {

    @Resource
    FollowService followService;

    @Resource
    ControllerUtils utils;

    @GetMapping("/or")
    public RestBean<Boolean> isFollow(@RequestParam("id") @Min(0) int id, @RequestAttribute(Const.ATTR_USER_ID)int uid){
        boolean follow = followService.isFollow(id, uid);
        return RestBean.success(follow);
    }
    @GetMapping()
    public RestBean<Void> followById(@RequestParam("id") @Min(0) int id,@RequestParam("type")boolean type,
        @RequestAttribute(Const.ATTR_USER_ID)int uid){
        return  utils.messageHandle(() ->
            followService.followById(id, type,uid));
    }
    @GetMapping("/list")
    public RestBean<List<Integer>> followList(@RequestAttribute(Const.ATTR_USER_ID)int uid){
        List<Integer> list = followService.followList(uid);
        return RestBean.success(list);
    }
}
