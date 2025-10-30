package com.example.entity.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AccountPrivacyRespDTO {
    private int id;
    boolean phone = true;
    boolean email = true;
    boolean wx = true;
    boolean qq = true;
    boolean gender = true;
}
