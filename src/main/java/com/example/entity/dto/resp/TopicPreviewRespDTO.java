package com.example.entity.dto.resp;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class TopicPreviewRespDTO {
    int id;
    int type;
    String title;
    String text;
    List<String> images;
    Date time;
    Integer uid;
    Integer tid;
    String username;
    String avatar;
    int like;
    int collect;
}
