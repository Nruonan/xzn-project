package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.req.RemoveTicketOrderReqDTO;
import com.example.entity.dto.req.TicketOrderRepeatReqDO;
import com.example.entity.dto.req.TicketOrderReqDO;
import com.example.entity.dto.req.addTicketReqDTO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketOrderRespDTO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.entity.dto.resp.TicketTypeRespDTO;
import com.example.service.TicketService;
import com.example.utils.Const;
import com.example.utils.ControllerUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Resource
    TicketService ticketService;

    @Resource
    ControllerUtils utils;

    @PostMapping("/add-ticket")
    public RestBean<Void> addTicket(@RequestBody @Valid addTicketReqDTO requestParam,@RequestAttribute(Const.ATTR_USER_ID)int id){
        return utils.messageHandle(() ->
            ticketService.addTicket(requestParam,id));
    }

    @GetMapping("/list")
    public RestBean<TicketCountRespDTO> ticketList(@RequestParam String name, @RequestParam int type,@RequestParam @Min(0) @Max(10) int page){
        return RestBean.success(ticketService.ticketList(name, type,page + 1));
    }

    @GetMapping("/types")
    public RestBean<List<TicketTypeRespDTO>> ticketTypeList(){
        return RestBean.success(ticketService.ticketTypeList());
    }

    @GetMapping()
    public RestBean<TicketRespDTO> findTicketById(@RequestParam int id){
        return RestBean.success(ticketService.findTicketById(id));
    }
    @PostMapping("/save-order")
    public RestBean<Void> saveTicketOrder(@Valid @RequestBody TicketOrderReqDO requestParam,@RequestAttribute(Const.ATTR_USER_ID) int id){
        return utils.messageHandle(() ->
            ticketService.saveTicketOrder(requestParam,id));
    }

    @PostMapping("/order-repeat")
    public RestBean<Void> saveTicketOrderRepeat(@Valid @RequestBody TicketOrderRepeatReqDO requestParam){
        return utils.messageHandle(() ->
            ticketService.saveTicketOrderRepeat(requestParam));
    }

    @GetMapping("/orders")
    public RestBean<List<TicketOrderRespDTO>> getTicketOrdersById(@RequestAttribute(Const.ATTR_USER_ID)int id, @RequestParam int uid){
        return RestBean.success(ticketService.getTicketOrdersById(id,uid));
    }

    @PostMapping("/remove-order")
    public RestBean<Void> removeTicketOrder(@RequestAttribute(Const.ATTR_USER_ID)int id, @RequestBody
        RemoveTicketOrderReqDTO requestParam){
        return utils.messageHandle(() ->
            ticketService.removeTicketOrder(id, requestParam));
    }
}
