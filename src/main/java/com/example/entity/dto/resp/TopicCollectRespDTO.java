package com.example.entity.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class TopicCollectRespDTO {
    int id;
    int type;
    String title;
    int uid;

}
