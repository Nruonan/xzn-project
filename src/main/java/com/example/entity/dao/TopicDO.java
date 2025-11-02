package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_topic")
public class TopicDO{
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String content;
    private Integer uid;
    private Integer type;
    private Integer top;
    private Date time;
    private Long score;
    private Integer status; // 0 表示草稿，1 表示已发布
}
