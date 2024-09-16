package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.Interact;
import com.example.entity.dao.TopicDO;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;



/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface TopicMapper extends BaseMapper<TopicDO> {
    @Insert("""
         <script>
            insert ignore into db_topic_interact_${type} values
            <foreach collection = "interacts" item="item" separator=",">
                (#{item.tid},#{item.uid},#{item.time})
            </foreach>
        </script>
    """)
    void addInteract(List<Interact> interacts, String type);


    @Delete("""
     <script>
                delete from db_topic_interact_${type} where
                <foreach collection="interacts" item="item" separator=" or ">
                    (tid = #{item.tid} and uid = #{item.uid})
                </foreach>
            </script>
    """)
    int deleteInteract(List<Interact> interacts, String type);
}
