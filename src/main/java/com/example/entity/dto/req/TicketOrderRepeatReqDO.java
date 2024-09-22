package com.example.entity.dto.req;

import jakarta.validation.constraints.Min;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class TicketOrderRepeatReqDO {
    Long id;
    Boolean pay;
}
