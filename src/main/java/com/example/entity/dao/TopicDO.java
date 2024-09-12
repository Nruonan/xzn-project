package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Date time;
}
