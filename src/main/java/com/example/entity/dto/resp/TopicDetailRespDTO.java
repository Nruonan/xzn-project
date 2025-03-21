package com.example.entity.dto.resp;


import java.util.Date;
import lombok.AllArgsConstructor;
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
   Long comments;
   User user;
   Interact interact;
   @Data
   @AllArgsConstructor
   public static class Interact{
       Boolean like;
       Boolean collect;
   }

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
