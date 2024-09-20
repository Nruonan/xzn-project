package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.NotificationDO;
import com.example.entity.dao.NotificationDO.NotificationDOBuilder;
import com.example.entity.dto.resp.NotificationRespDTO;
import com.example.mapper.NotificationMapper;
import com.example.service.NotificationService;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, NotificationDO> implements
    NotificationService {

    @Override
    public List<NotificationRespDTO> findUserNotification(int uid) {
        LambdaQueryWrapper<NotificationDO> eq = Wrappers.lambdaQuery(NotificationDO.class)
            .eq(NotificationDO::getUid, uid);
        List<NotificationDO> notificationDOS = baseMapper.selectList(eq);
        return BeanUtil.copyToList(notificationDOS, NotificationRespDTO.class);
    }

    @Override
    public void deleteUserNotification(int id, int uid) {
        LambdaQueryWrapper<NotificationDO> wrapper = Wrappers.lambdaQuery(NotificationDO.class)
            .eq(NotificationDO::getUid, uid)
            .eq(NotificationDO::getId, id);
        baseMapper.delete(wrapper);
    }

    @Override
    public void deleteUserAllNotification(int uid) {
        LambdaQueryWrapper<NotificationDO> wrapper = Wrappers.lambdaQuery(NotificationDO.class)
            .eq(NotificationDO::getUid, uid);
        baseMapper.delete(wrapper);
    }

    @Override
    public void addNotification(int uid, String title, String content, String type, String url) {
        NotificationDO builder = NotificationDO.builder()
            .content(content)
            .uid(uid)
            .url(url)
            .type(type)
            .title(title)
            .time(new Date())
            .build();
        this.save(builder);
    }
}
