package com.example.entity.dto.resp;

import com.example.entity.dto.resp.TopicDetailRespDTO.Interact;
import com.example.entity.dto.resp.TopicDetailRespDTO.User;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import java.util.Date;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class TopicInfoRespDTO {
    private Integer id;
    private String title;
    private String content;
    private Integer uid;
    private Integer type;
    private Date time;
    private Integer top;
}
