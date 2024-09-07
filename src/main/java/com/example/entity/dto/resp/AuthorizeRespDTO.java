package com.example.entity.dto.resp;

import java.util.Date;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class AuthorizeRespDTO {
    String username;
    String role;
    String token;
    Date expire;
}
