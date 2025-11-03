package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_topic_type")
public class TopicTypeDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    String name;
    @TableField("`desc`")
    String desc;
    String color;
}
