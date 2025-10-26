package com.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dao.NoticeDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.req.NoticeCreateReqDTO;
import com.example.entity.dto.req.NoticeUpdateReqDTO;
import com.example.entity.dto.resp.NoticeRespDTO;
import java.util.List;

/**
* @author Nruonan
* @description 针对表【db_notice(公告表)】的数据库操作Service
* @createDate 2025-10-23 22:38:48
*/
public interface NoticeService extends IService<NoticeDO> {
    /**
     * 创建公告
     */
    boolean createNotice(NoticeCreateReqDTO reqDTO, int authorId);

    /**
     * 更新公告
     */
    boolean updateNotice(NoticeUpdateReqDTO reqDTO);

    /**
     * 删除公告
     */
    boolean deleteNotice(Integer id);

    /**
     * 分页获取公告列表
     */
    Page<NoticeRespDTO> getNoticeList(int pageNum, int pageSize, String title);

    /**
     * 获取公告详情
     */
    NoticeRespDTO getNoticeDetail(Integer id);

    /**
     * 获取最新公告
     */
    NoticeRespDTO getNoticeOne();


}
