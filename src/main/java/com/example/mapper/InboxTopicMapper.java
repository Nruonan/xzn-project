package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.InboxTopicDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface InboxTopicMapper extends BaseMapper<InboxTopicDO> {
    @Select("""
        SELECT * FROM db_inbox_topic WHERE uid = fid
         AND uid IN (
            SELECT fid FROM db_follow 
            WHERE fid IN ( 
            SELECT DISTINCT f1.fid 
            FROM db_follow AS f1 
            WHERE f1.uid = #{id} 
            ) 
            GROUP BY fid HAVING COUNT(uid) >= 5
         )
    """)
    List<InboxTopicDO> selectBigVTopic(int id);
}
