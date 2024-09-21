package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dao.TicketTypeDO;
import com.example.entity.dto.resp.TicketCountRespDTO;
import com.example.entity.dto.resp.TicketRespDTO;
import com.example.entity.dto.resp.TicketTypeRespDTO;
import com.example.service.TicketService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/list")
    public RestBean<TicketCountRespDTO> ticketList(@RequestParam String name, @RequestParam int type,@RequestParam @Min(0) @Max(10) int page){
        return RestBean.success(ticketService.ticketList(name, type,page + 1));
    }

    @GetMapping("/types")
    public RestBean<List<TicketTypeRespDTO>> ticketTypeList(){
        return RestBean.success(ticketService.ticketTypeList());
    }
}
