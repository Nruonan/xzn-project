package com.example.entity.dto.resp;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class TicketOrderRespDTO {
    String id;
    Integer tid;
    String name;
    String desc;
    Long count;
    float price;
    Date time;
    Boolean pay;
}
