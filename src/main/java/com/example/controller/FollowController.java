package com.example.controller;


import com.example.config.user.UserContext;
import com.example.entity.RestBean;
import com.example.entity.dto.resp.FansDetailRespDTO;
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
    public RestBean<Void> followById(@RequestParam("id") @Min(0) int id,
        @RequestAttribute(Const.ATTR_USER_ID)int uid){
        return  utils.messageHandle(() ->
            followService.followById(id,uid));
    }
    @GetMapping("/list")
    public RestBean<List<Integer>> followList(@RequestAttribute(Const.ATTR_USER_ID)int uid){
        List<Integer> list = followService.followList(uid);
        return RestBean.success(list);
    }

    @GetMapping("/fans")
    public RestBean<Integer> findFansById(@RequestParam("id") @Min(0) int id){
        return RestBean.success(followService.findFansById(id));
    }
    @GetMapping("/follows")
    public RestBean<Integer> findFollowsById(@RequestParam("id") @Min(0) int id){
        return  RestBean.success(followService.findFollowsById(id));
    }

    @GetMapping("/fans-list")
    public RestBean<List<FansDetailRespDTO>> fansList(){
        return RestBean.success(followService.fansList(UserContext.getUserId()));
    }
    @GetMapping("/follow-list")
    public RestBean<List<FansDetailRespDTO>> followList(){
        return RestBean.success(followService.followsList(UserContext.getUserId()));
    }

    @GetMapping("/together")
    public RestBean<List<FansDetailRespDTO>> findTogether(@RequestParam("id") @Min(1) int id){
        return RestBean.success(followService.findTogether(UserContext.getUserId(),id));
    }
}
