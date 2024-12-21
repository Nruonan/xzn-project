package com.example.service.impl;

import static com.example.utils.Const.FANS_CACHE;
import static com.example.utils.Const.FOLLOWS_CACHE;
import static com.example.utils.Const.FOLLOW_CACHE;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dao.FollowDO;
import com.example.entity.dto.resp.FansDetailRespDTO;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.FollowMapper;
import com.example.mapper.InboxTopicMapper;
import com.example.mapper.TopicMapper;
import com.example.service.FollowService;
import com.example.utils.CacheUtils;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Resource
    AccountMapper accountMapper;
    @Resource
    AccountDetailsMapper detailsMapper;
    @Resource
    CacheUtils cacheUtils;
    @Resource
    RabbitTemplate rabbitTemplate;
    @Override
    public boolean isFollow(int id, int uid) {
        Long count = query().eq("uid", uid)
            .eq("fid", id).eq("status",1).count();
        //只想知道有没有，所以用count(*)即可
        return count > 0;
    }


    @Override
    @Transactional
    public String followById(int id,int uid) {
        if (id == uid)return "关注错误，关注用户为当前用户！";
        String key = FOLLOW_CACHE + uid;
        Long count = stringRedisTemplate.opsForZSet().size(key);
        if (count == 0){
            count = baseMapper.selectCount(new LambdaQueryWrapper<>(FollowDO.class)
                .eq(FollowDO::getUid, uid));
        }
        LambdaQueryWrapper<FollowDO> eq = new LambdaQueryWrapper<>(FollowDO.class)
            .in(FollowDO::getUid, uid)
            .eq(FollowDO::getFid,id);
        FollowDO followDO = baseMapper.selectOne(eq);
        HashMap<String, Object> map = new HashMap<>();
        map.put("count",count);
        map.put("follow",followDO);
        map.put("key",key);
        map.put("id",id);
        map.put("uid",uid);
        rabbitTemplate.convertAndSend("follow.direct","follow",map);
        return null;
    }

    @Override
    public List<Integer> followList(int uid) {
        LambdaQueryWrapper<FollowDO> eq = new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, uid).eq(FollowDO::getStatus,1);
        List<FollowDO> followDOS = baseMapper.selectList(eq);
        List<Integer> collect = followDOS.stream()
            .map(FollowDO::getFid)
            .toList();
        return collect;
    }

    @Override
    public Integer findFansById(int id) {
        Long fans = baseMapper.selectCount(new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getFid, id).eq(FollowDO::getStatus,1));
        if(fans > 0){
            return Math.toIntExact(fans);
        }
        return 0;
    }

    @Override
    public Integer findFollowsById(int id) {
        Long follows = baseMapper.selectCount(new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, id).eq(FollowDO::getStatus,1));
        if(follows > 0){
            return Math.toIntExact(follows);
        }
        return 0;
    }

    @Override
    public List<FansDetailRespDTO> fansList(Integer userId) {
        List<FansDetailRespDTO> fansDetailRespDTOS = cacheUtils.takeListFormCache(FANS_CACHE + userId,
            FansDetailRespDTO.class);
        if (fansDetailRespDTOS != null){
            return fansDetailRespDTOS;
        }
        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getFid, userId)
            .eq(FollowDO::getStatus,1);
        List<FollowDO> followDOS = baseMapper.selectList(wrapper);
        if (followDOS.size() == 0){
            cacheUtils.saveListToCache(FANS_CACHE + userId, null, 60*60);
            return null;
        }
        List<Integer> collect = followDOS.stream()
            .map(FollowDO::getUid)
            .toList();
        // 创建 LambdaQueryWrapper 对象
        LambdaQueryWrapper<AccountDO> wrapper2 = new LambdaQueryWrapper<>(AccountDO.class);
        // 设置查询条件，使用 in 方法指定 id 列表
        wrapper2.in(AccountDO::getId, collect);
        List<AccountDO> accountDOS = accountMapper.selectList(wrapper2);
        fansDetailRespDTOS = accountDOS.stream()
            .map(accountDO -> {
                FansDetailRespDTO dto = new FansDetailRespDTO();
                // 假设 AccountDO 和 FansDetailRespDTO 有对应的字段
                dto.setId(accountDO.getId());
                dto.setUsername(accountDO.getUsername());
                dto.setAvatar(accountDO.getAvatar());
                return dto;
            })
            .collect(Collectors.toList());
        cacheUtils.saveListToCache(FANS_CACHE + userId, fansDetailRespDTOS, 60);
        return fansDetailRespDTOS;
    }

    @Override
    public List<FansDetailRespDTO> followsList(Integer userId) {
        List<FansDetailRespDTO> fansDetailRespDTOS = cacheUtils.takeListFormCache(FOLLOWS_CACHE + userId,
            FansDetailRespDTO.class);
        if (fansDetailRespDTOS != null){
            return fansDetailRespDTOS;
        }
        LambdaQueryWrapper<FollowDO> wrapper = new LambdaQueryWrapper<>(FollowDO.class)
            .eq(FollowDO::getUid, userId)
            .eq(FollowDO::getStatus,1);
        List<FollowDO> followDOS = baseMapper.selectList(wrapper);
        if (followDOS.size() == 0){
            cacheUtils.saveListToCache(FANS_CACHE + userId, null, 60*60);
            return null;
        }
        List<Integer> collect = followDOS.stream()
            .map(FollowDO::getFid)
            .toList();
        // 创建 LambdaQueryWrapper 对象
        LambdaQueryWrapper<AccountDO> wrapper2 = new LambdaQueryWrapper<>(AccountDO.class);
        // 设置查询条件，使用 in 方法指定 id 列表
        wrapper2.in(AccountDO::getId, collect);
        List<AccountDO> accountDOS = accountMapper.selectList(wrapper2);
        fansDetailRespDTOS = accountDOS.stream()
            .map(accountDO -> {
                FansDetailRespDTO dto = new FansDetailRespDTO();
                // 假设 AccountDO 和 FansDetailRespDTO 有对应的字段
                dto.setId(accountDO.getId());
                dto.setUsername(accountDO.getUsername());
                dto.setAvatar(accountDO.getAvatar());
                return dto;
            })
            .collect(Collectors.toList());
        cacheUtils.saveListToCache(FOLLOWS_CACHE + userId, fansDetailRespDTOS, 60);
        return fansDetailRespDTOS;
    }

    @Override
    public List<FansDetailRespDTO> findTogether(Integer userId, Integer id) {
        // TODO 共同关注
        return null;
    }
}
