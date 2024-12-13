package com.example.service.impl;

import com.example.entity.dto.resp.DataRespDTO;
import com.example.service.DataService;
import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class DataServiceImpl implements DataService {


    @Resource
    StringRedisTemplate stringRedisTemplate;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public DataRespDTO getCount() {
        String date = df.format(new Date());
        Long dau= stringRedisTemplate.opsForHyperLogLog().size("xzn:dau:" + date);
        Long uv = stringRedisTemplate.opsForHyperLogLog().size("xzn:uv:" + date);
        return new DataRespDTO(uv,dau);
    }
}
