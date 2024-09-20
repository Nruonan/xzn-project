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
public class NotificationRespDTO {
    Integer id;
    Integer uid;
    String title;
    String content;
    String type;
    String url;
    Date time;
}
