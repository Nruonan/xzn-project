package com.example.entity.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Valid
public class ChangePassWordReqDTO {

    String password;
    @Length(max = 16 ,min = 6)
    String new_password;
    String new_repeat_password;
}
