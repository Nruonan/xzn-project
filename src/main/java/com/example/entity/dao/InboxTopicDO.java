package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@TableName("db_inbox_topic")
@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class InboxTopicDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer tid;
    Integer uid;
    Integer fid;
    String title;
    String content;
    Integer type;
    Date time;
}
