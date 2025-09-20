package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.TicketDO;
import com.example.entity.dao.TicketOrderDO;
import com.example.mapper.TicketMapper;
import com.example.mapper.TicketOrderMapper;
import com.example.service.TicketOrderService;
import com.example.service.TicketService;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class TicketOrderServiceImpl extends ServiceImpl<TicketOrderMapper, TicketOrderDO> implements
    TicketOrderService {

}
