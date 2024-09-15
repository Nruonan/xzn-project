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
public class TopTopicRespDTO {
    int id;
    String title;
    Date time;
}
