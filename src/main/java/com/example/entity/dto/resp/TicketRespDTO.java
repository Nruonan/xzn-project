package com.example.entity.dto.resp;


import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@Data
public class TicketRespDTO {
    Integer id;
    @Length(min = 20)
    String name;
    @Length(min = 40)
    String desc;
    Integer type;
    Integer validDateType;
    Date validDate;
    @Min(0)
    Float price;
    @Min(0)
    Long count;

    Date createTime;

}
