package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_ticket_type")
public class TicketTypeDO {
    @TableId(type = IdType.AUTO)
    Integer id;

    String name;
}
