package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@TableName("db_ticket_order")
@Data
public class TicketOrderDO {
    @TableId
    Long id;
    Integer uid;
    Integer tid;
    Long count;
    float price;
    Date time;
    Boolean pay;
}
