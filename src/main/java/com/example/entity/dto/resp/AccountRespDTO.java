package com.example.entity.dto.resp;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRespDTO {
    private Integer id;
    private String username;
    private String password;
    private String role;
    private boolean mute;
    private boolean banned;
}
