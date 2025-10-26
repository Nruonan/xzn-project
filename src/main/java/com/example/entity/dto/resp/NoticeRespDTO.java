package com.example.entity.dto.resp;

import com.example.entity.dao.AccountDetailsDO;
import lombok.Data;
import java.util.Date;

@Data
public class NoticeRespDTO {

    private Integer id;
    private String title;
    private String content;
    private Date publishTime;
    private Integer status;
    private Integer uid;
    private String username;
    private String avatar;
}