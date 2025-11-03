package com.example.entity.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xzn
 * @create 2025/11/3
 * @desc
 */
@AllArgsConstructor
@Data
public class TopicTypeReqDTO {
    private Integer id;
    private String name;
    private String desc;
    private String color;
}
