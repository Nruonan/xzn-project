package com.example.entity.dto.req;

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
@AllArgsConstructor
public class TicketOrderReqDO {
    @Min(1)
    Integer uid;
    @Min(1)
    Integer tid;
    @Min(1)
    Long count;
    @Min(0)
    float price;
    Boolean pay;
}
