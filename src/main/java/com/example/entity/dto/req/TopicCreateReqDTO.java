package com.example.entity.dto.req;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@Data
public class TopicCreateReqDTO {
    @Length(min = 0,max = 30)
    private String title;

    private JSONObject content;
    @Min(1)
    private int type;
}
