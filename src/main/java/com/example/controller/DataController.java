package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.resp.DataRespDTO;
import com.example.service.DataService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/data")
public class DataController {

    @Resource
    DataService dataService;

    @GetMapping("/count")
    public RestBean<DataRespDTO> getCount(){
        return RestBean.success(dataService.getCount());
    }

}
