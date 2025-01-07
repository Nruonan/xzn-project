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
@TableName("db_account")
@AllArgsConstructor
public class AccountDO {
    @TableId(type= IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String avatar;
    private String email;
    private String role;
    private Date registerTime;
    private boolean mute;
    private boolean banned;
}
