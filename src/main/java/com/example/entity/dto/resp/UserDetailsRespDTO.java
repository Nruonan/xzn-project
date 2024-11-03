package com.example.entity.dto.resp;

import com.example.entity.dao.TopicDO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Nruonan
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetailsRespDTO {
    Integer id;
    String username;
    String avatar;
    String desc;
    Integer gender;
    String qq;
    String wx;
    String phone;
    String email;
    Integer follow;
    List<TopicDO> topics;
}
