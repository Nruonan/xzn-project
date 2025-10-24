package com.example.entity.dto.req;

import lombok.Data;

@Data
public class NoticeUpdateReqDTO {
    private Integer id;
    private String title;
    private String content;
    private Integer status;
}