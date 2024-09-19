package com.example.entity.dto.resp;

import com.example.entity.dto.resp.TopicDetailRespDTO.Interact;
import com.example.entity.dto.resp.TopicDetailRespDTO.User;
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
public class CommentRespDTO {
    Integer id;
    String content;
    Date time;
    String quote;
    String root;
    User user;
    String quoteName;
    //子评论
    private List<CommentRespDTO> children;
    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        String desc;
        Integer gender;
        String qq;
        String wx;
        String phone;
        String email;
    }
}
