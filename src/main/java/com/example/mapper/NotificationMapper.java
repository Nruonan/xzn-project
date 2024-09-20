package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.NotificationDO;
import jakarta.validation.constraints.Min;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface NotificationMapper extends BaseMapper<NotificationDO> {

}
