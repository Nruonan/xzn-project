package com.example.entity.dto.resp;

import com.example.entity.dao.TicketTypeDO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
@AllArgsConstructor
public class TicketCountRespDTO {
    List<TicketRespDTO> list;
    Long tickets;
}
