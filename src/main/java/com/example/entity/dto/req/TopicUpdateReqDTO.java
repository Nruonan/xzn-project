package com.example.entity.dto.req;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class TopicUpdateReqDTO {
    @Min(0)
    int id;
    @Min(1)
    @Max(5)
    int type;

    @Length(min = 1, max = 30)
    String title;

    JSONObject content;
    int status;
}
