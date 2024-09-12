package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dao.WeatherDO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.service.TopicService;
import com.example.service.WeatherService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/weather")
    public RestBean<WeatherDO> weather(double longitude, double latitude){
        WeatherDO weatherDO = weatherService.fetchWeather(longitude, latitude);
        return weatherDO == null ? RestBean.failure(400,"获取地理位置信息与天奇失败，请联系管理员!") : RestBean.success(weatherDO);
    }

    @GetMapping("/types")
    public RestBean<List<TopicTypeRespDTO>> listTypes(){
        return RestBean.success(topicService.listTypes());
    }
}
