package com.example.entity.dto.req;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@AllArgsConstructor
@Data
public class EmailResetReqDTO {
    @Email
    String email;
    @Length(min = 6,max = 6)
    String code;
    @Length(min = 6,max = 20)
    String password;
}
