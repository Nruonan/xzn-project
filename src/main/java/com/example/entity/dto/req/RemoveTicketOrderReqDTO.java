package com.example.entity.dto.req;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @author Nruonan
 * @description
 */
@AllArgsConstructor
@Data
public class RemoveTicketOrderReqDTO {
    @Min(1)
    int uid;
    @Length(min = 9)
    Long id;
}
