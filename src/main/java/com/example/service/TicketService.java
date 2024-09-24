package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dao.TicketDO;
import com.example.entity.dto.req.RemoveTicketOrderReqDTO;
import com.example.entity.dto.req.TicketOrderRepeatReqDO;
import com.example.entity.dto.req.TicketOrderReqDO;
import com.example.entity.dto.req.addTicketReqDTO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketOrderRespDTO;
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

    String saveTicketOrder(TicketOrderReqDO requestParam, int id);
    String saveTicketOrderRepeat(TicketOrderRepeatReqDO requestParam);
    TicketRespDTO findTicketById(int id);

    List<TicketOrderRespDTO> getTicketOrdersById(int id, int uid);


    String removeTicketOrder(int id, RemoveTicketOrderReqDTO requestParam);

    String addTicket(addTicketReqDTO requestParam, int id);
}
