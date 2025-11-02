package com.example.entity.dto.resp;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class HotTopicRespDTO {
    int id;
    String title;
    Date time;
    Integer uid;
    String username;
    String avatar;
    Long score;
}
