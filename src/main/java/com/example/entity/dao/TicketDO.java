package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_market_ticket")
@AllArgsConstructor
public class TicketDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    String name;
    @TableField("`desc`")
    String desc;
    Integer type;
    Integer validDateType;
    Date validDate;
    Double price;
    Integer count;
    Date createTime;

}
