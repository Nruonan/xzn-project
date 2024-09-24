package com.example.entity.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Date;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author Nruonan
 * @description
 */
@Data
public class addTicketReqDTO {
    String name;
    @TableField("`desc`")
    String desc;
    @Min(0) @Max(4)
    Integer type;
    @Min(0) @Max(1)
    Integer validDateType;

    Date validDate;
    @Min(0)
    Double price;
    @Min(1)
    Integer count;
}
