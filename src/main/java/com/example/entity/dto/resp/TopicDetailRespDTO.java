package com.example.entity.dto.resp;


import java.util.Date;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class TopicDetailRespDTO {
   Integer id;
   String title;
   String content;
   Integer type;
   Date time;
   User user;


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
