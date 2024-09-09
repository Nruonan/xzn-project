package com.example.entity.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

/**
 * @author Nruonan
 * @description
 */
@Data
@Validated
@AllArgsConstructor
@TableName("db_account_details")
@NoArgsConstructor
@Builder
public class AccountDetailsDO {
    @TableId()
    private Integer id;

    private Integer gender;
    private String phone;
    private String qq;
    private String wx;
    @TableField("`desc`")
    private String desc;
}
