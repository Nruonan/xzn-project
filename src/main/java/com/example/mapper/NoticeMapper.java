package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.dao.NoticeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
* @author Nruonan
* @description 针对表【db_notice(公告表)】的数据库操作Mapper
* @createDate 2025-10-23 22:38:48
* @Entity com.example.entity.dao.NoticeDO
*/
@Mapper
public interface NoticeMapper extends BaseMapper<NoticeDO> {
    /**
     * 分页查询公告列表（只查询已发布的公告）
     */
    IPage<NoticeDO> selectTopNotices(Page<NoticeDO> page);

    /**
     * 查询置顶公告
     */
    @Select("SELECT * FROM db_notice WHERE status = 2 ORDER BY  publish_time DESC LIMIT #{limit}")
    java.util.List<NoticeDO> selectTopNotices(@Param("limit") int limit);
}




