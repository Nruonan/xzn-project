package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@TableName("db_inbox_topic")
@AllArgsConstructor
@Data
public class InboxTopicDO {
    Integer id;
    Integer tid;
    Integer uid;
    Integer fid;
    String title;
    String content;
    Integer type;
    Date time;
}
