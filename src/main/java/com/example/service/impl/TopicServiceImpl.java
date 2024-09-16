package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dao.Interact;
import com.example.entity.dao.TopicDO;
import com.example.entity.dao.TopicTypeDO;
import com.example.entity.dto.req.TopicCreateReqDTO;
import com.example.entity.dto.resp.TopTopicRespDTO;
import com.example.entity.dto.resp.TopicDetailRespDTO;
import com.example.entity.dto.resp.TopicDetailRespDTO.User;
import com.example.entity.dto.resp.TopicPreviewRespDTO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.mapper.TopicMapper;
import com.example.mapper.TopicTypeMapper;
import com.example.service.AccountPrivacyService;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
@Slf4j
public class TopicServiceImpl extends ServiceImpl<TopicMapper, TopicDO> implements TopicService {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

    @Resource
    RedissonClient redissonClient;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    private Set<Integer> types = null;
    @PostConstruct
    private void initTypes() {
        types = this.listTypes()
            .stream()
            .map(TopicTypeRespDTO::getId)
            .collect(Collectors.toSet());
    }
    @Override
    public List<TopicTypeRespDTO> listTypes() {
        List<TopicTypeRespDTO> topicTypeRespDTOS = BeanUtil.copyToList(topicTypeMapper.selectList(null),
            TopicTypeRespDTO.class);
        return topicTypeRespDTOS;
    }
    /**
     * @param requestParam 帖子属性
     * 创建帖子
     */
    @Override
    public String createTopic(TopicCreateReqDTO requestParam, int uid) {
        if (!this.textLimitCheck(requestParam.getContent(),20000)) {
            return "文章内容太多，发文失败！";
        }
        if (!types.contains(requestParam.getType())) {
            return "文章类型非法";
        }
        String key = Const.FORUM_TOPIC_CREATE_COUNTER + uid;
        if (!flowUtils.limitPeriodCounterCheck(key, 3, 3600)){
            return "发文频繁,请稍后再试";
        }
        TopicDO topic = BeanUtil.toBean(requestParam, TopicDO.class);
        topic.setContent(requestParam.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if(this.save(topic)){
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            return null;
        }else{
            return "内部错误，请联系管理员!";
        }

    }

    @Override
    public List<TopicPreviewRespDTO> listTopicByPage(int pageNumber, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + ":" +  type;
        String lockKey = "lock:" + key;
        // 从缓存中获取数据
        List<TopicPreviewRespDTO> list = cacheUtils.takeListFormCache(key, TopicPreviewRespDTO.class);
        if (list != null) return list;
        RLock lock = redissonClient.getLock(lockKey);
        Page<TopicDO> page = new Page<>(pageNumber , 10);
        try{
            if (!lock.tryLock()){
                // 再次检查缓存是否已经被其他线程更新
                list = cacheUtils.takeListFormCache(key, TopicPreviewRespDTO.class);
                if (list != null) {
                    return list;
                }
            }
            if (type == 0){
                baseMapper.selectPage(page, Wrappers.lambdaQuery(TopicDO.class).orderByDesc(TopicDO::getTime));
            }else{
                baseMapper.selectPage(page,Wrappers.lambdaQuery(TopicDO.class).eq(TopicDO::getType,type).orderByDesc(TopicDO::getTime));
            }

            List<TopicDO> topics = page.getRecords();
            if (topics.isEmpty()) {
                // 将空值也存入缓存，避免缓存穿透
                cacheUtils.saveListToCache(key, new ArrayList<>(), 60);
                return new ArrayList<>();
            }
            list = topics.stream().map(this::resolveToPreview).toList();
            cacheUtils.saveListToCache(key , list, 60);
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();  // 释放锁
        }
        return list;
    }

    @Override
    public List<TopTopicRespDTO> listTopTopics() {
        List<TopicDO> topicDOS = baseMapper.selectList(Wrappers.<TopicDO>query().select("id","title","time")
            .eq("top",1));
        return topicDOS.stream().map(topic -> {
            return BeanUtil.toBean(topic, TopTopicRespDTO.class);
        }).toList();
    }

    @Override
    public TopicDetailRespDTO getTopic(int tid) {
        // 查询文章信息
        TopicDO topic = baseMapper.selectById(tid);
        // 克隆到文章详细对象
        TopicDetailRespDTO topicDetailRespDTO = BeanUtil.toBean(topic,TopicDetailRespDTO.class);
        TopicDetailRespDTO.User user = new TopicDetailRespDTO.User();
        TopicDetailRespDTO.Interact interact = new TopicDetailRespDTO.Interact(
            hasInteract(tid,topic.getUid(),"like"),
            hasInteract(tid,topic.getUid(),"collect")
        );
        topicDetailRespDTO.setInteract(interact);
        topicDetailRespDTO.setUser(this.fillUserDetailByPrivacy(user,topic.getUid()));
        return topicDetailRespDTO;
    }

    @Override
    public void interact(Interact interact, boolean state) {
        String type = interact.getType();
        synchronized (type.intern()){
            stringRedisTemplate.opsForHash().put(type,interact.toKey(),Boolean.toString(state));
            this.saveInteractSchedule(type);
        }
    }

    private boolean hasInteract(int tid, int uid, String type){
        String key = tid + ":" + uid;
        if (stringRedisTemplate.opsForHash().hasKey(type, key)) {
            return Boolean.parseBoolean(stringRedisTemplate.opsForHash().entries(type).get(key).toString());
        }
        return baseMapper.userInteractCount(tid,uid,type) > 0;
    }

    private final Map<String, Boolean> state = new HashMap<>();
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private void saveInteractSchedule(String type){
        // 获取map是否有值
        if (!state.getOrDefault(type,false)){
            // 设置类型存入为true
            state.put(type, true);
            service.schedule(() ->{
                // 存入数据到数据库
                this.saveInteract(type);
                // 删除
                state.put(type,false);
            },3, TimeUnit.SECONDS);
        }
    }
    private void saveInteract(String type){
        // 上锁对点赞还是收藏
        synchronized (type.intern()) {
            // 创建两个链表接受数据
            List<Interact> check = new LinkedList<>();
            List<Interact> uncheck = new LinkedList<>();
            stringRedisTemplate.opsForHash().entries(type).forEach((k, v) -> {
                // 从redis中获取数据
                if (Boolean.parseBoolean(v.toString())) {
                    // 根据bool值添加到相应的链表
                    check.add(Interact.parseInteract(k.toString(), type));
                } else {
                    uncheck.add(Interact.parseInteract(k.toString(), type));
                }
            });
            // 存在数据库
            if (!check.isEmpty()) {
                baseMapper.addInteract(check, type);
            }
            if (!uncheck.isEmpty()) {
                baseMapper.deleteInteract(uncheck, type);
            }
            // 删除redis数据
            stringRedisTemplate.delete(type);
        }
    }
    private <T> T fillUserDetailByPrivacy(T target, int uid){
        AccountDO accountDO = accountMapper.selectById(uid);
        AccountDetailsDO accountDetailsDO = accountDetailsMapper.selectById(uid);
        AccountPrivacyDO accountPrivacyDO = accountPrivacyMapper.selectById(uid);
        // 获取隐藏不要的数据
        String[] strings = accountPrivacyDO.hiddenFields();
        // 克隆除了隐藏的数据
        BeanUtils.copyProperties(accountDO,target,strings);
        BeanUtils.copyProperties(accountDetailsDO,target,strings);
        return target;

    }
    // 解析帖子
    private TopicPreviewRespDTO resolveToPreview(TopicDO topicDO){
        // 获取帖子
        TopicPreviewRespDTO bean = new TopicPreviewRespDTO();
        // 得到user属性
        BeanUtils.copyProperties(accountMapper.selectById(topicDO.getUid()),bean);
        // 得到帖子属性
        BeanUtils.copyProperties(topicDO, bean);
        bean.setLike(baseMapper.interactCount(topicDO.getId(),"like"));
        bean.setCollect(baseMapper.interactCount(topicDO.getId(),"collect"));
        // 获取点赞收藏
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topicDO.getContent()).getJSONArray("ops");
        for(Object op : ops){
            Object insert = JSONObject.from(op).get("insert");
            // 如果是String 就是普通句子用text存储
            if (insert instanceof String text){
                if (previewText.length() >= 300)continue;;
                previewText.append(text);
            }else if (insert instanceof Map<?,?> map){
                // 图片则用list存储
                Optional.ofNullable(map.get("image")).ifPresent(obj -> images.add(obj.toString()));
            }
        }
        bean.setText(previewText.length() > 300 ? previewText.substring(0, 300) :  previewText.toString());
        bean.setImages(images);
        return bean;
    }

    /**
     * @param object json字数
     * 校验字数是否小于20000
     */
    private boolean textLimitCheck(JSONObject object, int max){
        if (object == null)return false;
        long length = 0;
        for (Object op : object.getJSONArray("ops")) {
            length += JSONObject.from(op).getString("insert").length();
            if (length > max)return false;
        }
        return true;
    }
}
