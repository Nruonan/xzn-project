package com.example.entity.dto.req;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@AllArgsConstructor
@Data
public class RemoveTicketOrderReqDTO {
    @Min(1)
    int uid;
    @Min(1)
    Long id;
}
