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
public class AccountInfoRespDTO {
    private int id;
    private String username;
    private String email;
    private String role;
    String avatar;
    private Date registerTime;
    private boolean mute;
    private boolean banned;
}
