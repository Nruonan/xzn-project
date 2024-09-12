package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.TopicDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;



/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface TopicMapper extends BaseMapper<TopicDO> {
    @Select("""
        select * from db_topic left join db_account on uid = db_account.id
        order by `time` desc limit  ${page}
    """)
    List<TopicDO> topicList(int start);

    @Select("""
        select * from db_topic left join db_account on uid = db_account.id
        where type = #{type}
        order by `time` desc limit  ${page}
    """)
    List<TopicDO> topicListByType(int start,int type);

}
