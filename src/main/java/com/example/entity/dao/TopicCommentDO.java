package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
@TableName("db_topic_comment")
public class TopicCommentDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer tid;
    private String content;
    private Integer root;
    private Integer quote;
    private Date time;

}
