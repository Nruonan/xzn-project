package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.TicketDO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.entity.dto.resp.TicketTypeRespDTO;
import java.util.List;

/**
 * @author Nruonan
 * @description
 */
public interface TicketService extends IService<TicketDO> {

    TicketCountRespDTO ticketList(String name, int type,int pageNumber);

    List<TicketTypeRespDTO> ticketTypeList();
}
