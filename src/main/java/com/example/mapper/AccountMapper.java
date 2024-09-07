package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.dao.AccountDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Nruonan
 * @description
 */
@Mapper
public interface AccountMapper extends BaseMapper<AccountDO> {

}
