package com.example.entity.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountDetailsRespDTO {
    private Integer gender;
    private String qq;
    private String wx;
    private String phone;
    private String desc;
}
