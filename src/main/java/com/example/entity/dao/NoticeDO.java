package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 公告表
 * @TableName db_notice
 */
@TableName(value ="db_notice")
@Data
public class NoticeDO implements Serializable {
    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 发布者ID
     */
    private Integer uid;

    /**
     * 发布时间
     */
    private Date publishTime;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}