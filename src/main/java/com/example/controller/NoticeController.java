package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.resp.NoticeRespDTO;
import com.example.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    // TODO 管理端添加用户用户头像，信息， 客户端首页添加最新置顶公告，
    //  查询公告列表展示所有公告 和发表用户信息， 点开可以查看详情 用户名称 用户头像
    @Resource
    private NoticeService noticeService;

    /**
     * 获取最新公告
     */
    @GetMapping("/one")
    public RestBean<NoticeRespDTO> getNoticeOne() {
        return RestBean.success(noticeService.getNoticeOne());
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/detail")
    public RestBean<NoticeRespDTO> getNoticeDetail(Integer id) {
        return RestBean.success(noticeService.getNoticeDetail(id));
    }
}
