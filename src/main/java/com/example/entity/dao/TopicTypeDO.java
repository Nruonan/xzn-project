package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_topic_type")
public class TopicTypeDO {
    Integer id;
    String name;
    @TableField("`desc`")
    String desc;
}
