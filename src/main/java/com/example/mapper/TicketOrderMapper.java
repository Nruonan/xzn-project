package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.TicketOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface TicketOrderMapper extends BaseMapper<TicketOrderDO> {

}
