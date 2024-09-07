package com.example.entity.dto.req;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class ConfirmResetReqDTO {
    @Email
    String email;
    @Length(max = 6, min = 6)
    String code;
}
