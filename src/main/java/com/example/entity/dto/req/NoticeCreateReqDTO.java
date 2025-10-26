package com.example.entity.dto.req;

import lombok.Data;

@Data
public class NoticeCreateReqDTO {
    private String title;
    private String content;
    private Integer status;
}