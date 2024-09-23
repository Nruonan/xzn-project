package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.TicketOrderDO;
import com.example.entity.dto.resp.TicketOrderRespDTO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface TicketOrderMapper extends BaseMapper<TicketOrderDO> {

    @Update("""
        UPDATE  db_ticket_order o,db_market_ticket t
        SET t.count = o.count + t.count where o.tid = t.id
        AND o.pay = 0 AND o.id = #{id}
    """)
    int updateTicketCount(long id);

    @Delete("""
        DELETE FROM db_ticket_order WHERE pay = 0 AND  NOW() > DATE_ADD(time, INTERVAL 15 MINUTE ) AND id = #{id}
    """)
    int deleteTicketOrder(long id);

    @Select("""
        SELECT a.id, a.tid, a.count, a.price, a.time, a.pay, b.name, b.`desc` FROM db_ticket_order a left join db_market_ticket b on a.tid = b.id
        WHERE a.uid = #{id} ORDER BY(a.time) DESC;
    """)
    List<TicketOrderRespDTO> selectOrdersList(int id);
}
