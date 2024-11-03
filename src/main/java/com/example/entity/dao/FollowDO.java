package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Nruonan
 * @description
 */
@Data
@TableName("db_follow")
@AllArgsConstructor
@NoArgsConstructor
public class FollowDO {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer uid;
    Integer fid;
    Integer status;
    Date time;
}
