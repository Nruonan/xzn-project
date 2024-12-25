package com.example.entity.dto.resp;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class AuthorizeRefreshRespDTO {
    String username;
    String role;
    String access_token;
    String refresh_token;
    Date access_expire;
}
