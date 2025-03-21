package com.example.controller;

import com.alibaba.fastjson2.JSONObject;
import com.example.entity.RestBean;
import com.example.entity.dao.Interact;
import com.example.entity.dao.WeatherDO;
import com.example.entity.dto.req.AddCommentReqDTO;
import com.example.entity.dto.req.TopicCreateReqDTO;
import com.example.entity.dto.req.TopicUpdateReqDTO;
import com.example.entity.dto.resp.AccountInfoRespDTO;
import com.example.entity.dto.resp.CommentRespDTO;
import com.example.entity.dto.resp.HotTopicRespDTO;
import com.example.entity.dto.resp.TopTopicRespDTO;
import com.example.entity.dto.resp.TopicCollectRespDTO;
import com.example.entity.dto.resp.TopicDetailRespDTO;
import com.example.entity.dto.resp.TopicPreviewRespDTO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.service.AccountService;
import com.example.service.TopicService;
import com.example.service.WeatherService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Resource
    WeatherService weatherService;

    @Resource
    TopicService topicService;

    @Resource
    ControllerUtils utils;

    @Resource
    AccountService accountService;

    @GetMapping("/weather")
    public RestBean<WeatherDO> weather(double longitude, double latitude){
        WeatherDO weatherDO = weatherService.fetchWeather(longitude, latitude);
        return weatherDO == null ? RestBean.failure(400,"获取地理位置信息与天奇失败，请联系管理员!") : RestBean.success(weatherDO);
    }

    @GetMapping("/types")
    public RestBean<List<TopicTypeRespDTO>> listTypes(){
        return RestBean.success(topicService.listTypes());
    }

    @PostMapping("/create-topic")
    public RestBean<Void> createTopic(@Valid @RequestBody TopicCreateReqDTO requestParam, @RequestAttribute(Const.ATTR_USER_ID) int id){
        AccountInfoRespDTO account = accountService.findAccountById(id);
        if (account.isMute()){
            return RestBean.failure(400,"您的账户已被禁言，无法发布新的主题!");
        }
        return utils.messageHandle(() ->
            topicService.createTopic(requestParam,id));
    }

    @GetMapping("/list-topic")
    public RestBean<List<TopicPreviewRespDTO>> listTopic(@RequestParam @Min(0) @Max(10) int page, @RequestParam @Min(0) int type){
        return RestBean.success(topicService.listTopicByPage(page + 1,type));
    }
    @GetMapping("/list-topic-follow")
    public RestBean<List<TopicPreviewRespDTO>> listTopicFollow(@RequestParam @Min(0) @Max(10) int page,@RequestAttribute(Const.ATTR_USER_ID)int id){
        return RestBean.success(topicService.listTopicFollowByPage(page + 1,id));
    }
    @GetMapping("/top-topic")
    public RestBean<List<TopTopicRespDTO>> listTopTopics(){
        return RestBean.success(topicService.listTopTopics());
    }
    @GetMapping("/topic")
    public RestBean<TopicDetailRespDTO> topic(@RequestParam(value = "tid") @Min(0) int tid,@RequestAttribute(Const.ATTR_USER_ID)int uid){
        return RestBean.success(topicService.getTopic(tid,uid));
    }

    @GetMapping("/interact")
    public RestBean<Void> interact(@RequestParam @Min(0)int tid, @RequestParam @Pattern(regexp = "(like|collect)") String type,
        @RequestParam boolean state, @RequestAttribute(Const.ATTR_USER_ID) int id){
        topicService.interact(new Interact(tid, id, new Date(),type),state);
        return RestBean.success();
    }

    @GetMapping("/collects")
    public RestBean<List<TopicCollectRespDTO>> collects(@RequestAttribute(Const.ATTR_USER_ID) int id){
        return RestBean.success(topicService.getCollects(id));
    }

    @PostMapping("/update-topic")
    public RestBean<Void> updateTopic(@Valid @RequestBody TopicUpdateReqDTO requestParam,
        @RequestAttribute(Const.ATTR_USER_ID) int id){
        return utils.messageHandle(() ->
            topicService.updateTopic(requestParam,id));
    }

    @PostMapping("/add-comment")
    public RestBean<Void> addComment(@Valid @RequestBody AddCommentReqDTO requestParam,
                                     @RequestAttribute(Const.ATTR_USER_ID)int id){
        AccountInfoRespDTO account = accountService.findAccountById(id);
        if (account.isMute()){
            return RestBean.failure(400,"您的账户已被禁言，无法发布新的回复!");
        }
        return utils.messageHandle(() ->
            topicService.addComment(id,requestParam));
    }
    @GetMapping("/comments")
    public RestBean<List<CommentRespDTO>> comments(@RequestParam @Min(0) int tid,@RequestParam @Min(0) int page){
        return RestBean.success(topicService.comments(tid, page+ 1));
    }

    @GetMapping("/delete-comment")
    public RestBean<Void> deleteComment(@RequestAttribute(Const.ATTR_USER_ID)int id,@RequestParam @Min(0) int cid){
        return utils.messageHandle(() ->
            topicService.deleteComment(id,cid));
    }

    @GetMapping("/hot-topic")
    public RestBean<List<HotTopicRespDTO>> hotTopic(){
        return RestBean.success(topicService.hotTopic());
    }
}
