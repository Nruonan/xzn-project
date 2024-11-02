package com.example.service.impl;

import static com.example.utils.Const.FOLLOW_CACHE;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.FollowDO;
import com.example.mapper.FollowMapper;
import com.example.service.FollowService;
import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nruonan
 * @description
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, FollowDO> implements FollowService {

    @Resource
    StringRedisTemplate stringRedisTemplate;


    @Override
    public boolean isFollow(int id, int uid) {
        Long count = query().eq("uid", uid)
            .eq("fid", id).count();
        //只想知道有没有，所以用count(*)即可
        return count > 0;
    }


    @Override
    @Transactional
    public String followById(int id, boolean isFollow, int uid) {
        if (id == uid)return "关注错误，关注用户为当前用户！";
        String key = FOLLOW_CACHE + uid;
        if (!isFollow){
            FollowDO followDO = new FollowDO();
            followDO.setFid(id);
            followDO.setUid(uid);
            followDO.setTime(new Date());
            if(save(followDO)){
                stringRedisTemplate.opsForSet().add(key, String.valueOf(id));
                return null;
            }
        }else {
            boolean isSuccess = remove(new QueryWrapper<FollowDO>()
                .eq("uid", uid).eq("fid", id));
            if (isSuccess) {
                // 把关注用户的id从Redis集合中移除
                stringRedisTemplate.opsForSet().remove(key, String.valueOf(id));
            }
        }
        return null;
    }

    @Override
    public List<Integer> followList(int uid) {
        LambdaQueryWrapper<FollowDO> eq = new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, uid);
        List<FollowDO> followDOS = baseMapper.selectList(eq);
        List<Integer> collect = followDOS.stream()
            .map(FollowDO::getFid)
            .toList();
        return collect;
    }
}
