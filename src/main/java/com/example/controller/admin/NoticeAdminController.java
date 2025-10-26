package com.example.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.RestBean;
import com.example.entity.dto.req.NoticeCreateReqDTO;
import com.example.entity.dto.resp.NoticeRespDTO;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.example.entity.dto.req.NoticeUpdateReqDTO;
import com.example.service.NoticeService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notice")
public class NoticeAdminController {

    @Resource
    private NoticeService noticeService;

    /**
     * 创建公告
     */
    @PostMapping("/create")
    public RestBean<Boolean> createNotice(@RequestBody NoticeCreateReqDTO reqDTO,
        @RequestAttribute(Const.ATTR_USER_ID) int id) { // 假设 userId 通过拦截器注入
        return RestBean.success(noticeService.createNotice(reqDTO, id));
    }

    /**
     * 更新公告
     */
    @PostMapping("/update")
    public RestBean<Boolean> updateNotice(@RequestBody NoticeUpdateReqDTO reqDTO) {
        return RestBean.success(noticeService.updateNotice(reqDTO));
    }

    /**
     * 删除公告
     */
    @GetMapping("/delete")
    public RestBean<Boolean> deleteNotice(@RequestParam("id") Integer id) {
        return RestBean.success(noticeService.deleteNotice(id));
    }

    /**
     * 获取公告列表（分页）
     */
    @GetMapping("/list")
    public RestBean<Page<NoticeRespDTO>> getNoticeList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(value ="title", required = false) String title) {
        return RestBean.success(noticeService.getNoticeList(pageNum, pageSize, title));
    }

    /**
     * 获取单个公告详情
     */
    @GetMapping("/detail")
    public RestBean<NoticeRespDTO> getNoticeDetail(@RequestParam("id") Integer id) {
        return RestBean.success(noticeService.getNoticeDetail(id));
    }

}