package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.NoticeDO;
import com.example.entity.dto.req.NoticeCreateReqDTO;
import com.example.entity.dto.req.NoticeUpdateReqDTO;
import com.example.entity.dto.resp.NoticeRespDTO;
import com.example.mapper.AccountMapper;
import com.example.service.NoticeService;
import com.example.mapper.NoticeMapper;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
* @author Nruonan
* @description 针对表【db_notice(公告表)】的数据库操作Service实现
* @createDate 2025-10-23 22:38:48
*/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticeDO>
    implements NoticeService {
    @Resource
    private AccountMapper accountMapper;

    @Override
    public boolean createNotice(NoticeCreateReqDTO reqDTO, int authorId) {
        NoticeDO notice = new NoticeDO();
        notice.setTitle(reqDTO.getTitle());
        notice.setContent(reqDTO.getContent());
        notice.setUid(authorId);
        notice.setStatus(1); // 默认发布
        notice.setPublishTime(new Date());
        return this.save(notice);
    }

    @Override
    public boolean updateNotice(NoticeUpdateReqDTO reqDTO) {
        NoticeDO notice = this.getById(reqDTO.getId());
        if (notice == null) {
            return false;
        }

        notice.setTitle(reqDTO.getTitle());
        notice.setContent(reqDTO.getContent());

        if (reqDTO.getStatus() != null) {
            notice.setStatus(reqDTO.getStatus());
        }
        return this.updateById(notice);
    }

    @Override
    public boolean deleteNotice(Integer id) {
        return this.removeById(id);
    }

    @Override
    public Page<NoticeRespDTO> getNoticeList(int pageNum, int pageSize, String title) {
        Page<NoticeDO> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NoticeDO> wrapper = new LambdaQueryWrapper<>();


        if (title != null && !title.isEmpty()) {
            wrapper.like(NoticeDO::getTitle, title)
                .orderByDesc(NoticeDO::getPublishTime);
        } else {
            wrapper.orderByDesc(NoticeDO::getPublishTime);
        }
        IPage<NoticeDO> result = this.page(page, wrapper);

        Page<NoticeRespDTO> respPage = new Page<>();
        respPage.setCurrent(result.getCurrent());
        respPage.setSize(result.getSize());
        respPage.setTotal(result.getTotal());
        respPage.setRecords(result.getRecords().stream()
            .map(item -> {
                NoticeRespDTO respDTO = new NoticeRespDTO();
                BeanUtil.copyProperties(item, respDTO);
                return respDTO;
            })
            .collect(Collectors.toList()));

        return respPage;
    }

    @Override
    public NoticeRespDTO getNoticeDetail(Integer id) {
        NoticeDO notice = this.getById(id);
        if (notice == null || notice.getStatus() != 1) {
            return null;
        }

        return convertToRespDTO(notice);
    }

    @Override
    public List<NoticeRespDTO> getTopNotices(int limit) {
        List<NoticeDO> notices = this.baseMapper.selectTopNotices(limit);
        return notices.stream()
            .map(this::convertToRespDTO)
            .collect(Collectors.toList());
    }

    private NoticeRespDTO convertToRespDTO(NoticeDO notice) {
        NoticeRespDTO respDTO = new NoticeRespDTO();
        BeanUtil.copyProperties(notice, respDTO);

        // 设置作者信息
        AccountDO author = accountMapper.selectById(notice.getUid());
        if (author != null) {
            NoticeRespDTO.User user = new NoticeRespDTO.User();
            user.setId(author.getId());
            user.setUsername(author.getUsername());
            // 如果有头像字段，也可以设置
            user.setAvatar(author.getAvatar());
            respDTO.setUser(user);
        }

        return respDTO;
    }
}




