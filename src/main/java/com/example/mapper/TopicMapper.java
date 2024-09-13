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


}
