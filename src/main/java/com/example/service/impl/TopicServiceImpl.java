package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.AccountDO;
import com.example.entity.dao.AccountDetailsDO;
import com.example.entity.dao.AccountPrivacyDO;
import com.example.entity.dao.InboxTopicDO;
import com.example.entity.dao.Interact;
import com.example.entity.dao.TopicCommentDO;
import com.example.entity.dao.TopicDO;
import com.example.entity.dto.req.AddCommentReqDTO;
import com.example.entity.dto.req.TopicCreateReqDTO;
import com.example.entity.dto.req.TopicUpdateReqDTO;
import com.example.entity.dto.resp.CommentRespDTO;
import com.example.entity.dto.resp.CommentRespDTO.User;
import com.example.entity.dto.resp.TopTopicRespDTO;
import com.example.entity.dto.resp.TopicCollectRespDTO;
import com.example.entity.dto.resp.TopicDetailRespDTO;
import com.example.entity.dto.resp.TopicPreviewRespDTO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.mapper.AccountDetailsMapper;
import com.example.mapper.AccountMapper;
import com.example.mapper.AccountPrivacyMapper;
import com.example.mapper.TopicCommentMapper;
import com.example.mapper.TopicMapper;
import com.example.mapper.TopicTypeMapper;
import com.example.service.NotificationService;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    NotificationService notificationService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    TopicCommentMapper topicCommentMapper;

    @Resource
    RabbitTemplate rabbitTemplate;
    @Resource
    TopicMapper topicMapper;

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
        TopicDO topic = BeanUtil.toBean(requestParam, TopicDO.class);
        topic.setContent(requestParam.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if(this.save(topic)){
            // 生成唯一消息ID用于追踪
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            // 发送消息并配置持久化
            rabbitTemplate.convertAndSend(
                "topic.direct",
                "topic_follow",
                topic,
                message -> {
                    // 设置消息持久化（默认非持久化）
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                },
                correlationData
            );
            return null;
        }else{
            return "内部错误，请联系管理员!";
        }
    }
    @Override
    public String updateTopic(TopicUpdateReqDTO requestParam, int uid) {
        if (!this.textLimitCheck(requestParam.getContent(),20000)) {
            return "文章内容太多，发文失败！";
        }
        if (!types.contains(requestParam.getType())) {
            return "文章类型非法";
        }
        LambdaUpdateWrapper<TopicDO> wrapper = Wrappers.lambdaUpdate(TopicDO.class)
            .eq(TopicDO::getUid,uid)
            .eq(TopicDO::getId,requestParam.getId())
            .set(TopicDO::getTitle,requestParam.getTitle())
            .set(TopicDO::getStatus, requestParam.getStatus())
            .set(TopicDO::getContent,requestParam.getContent().toJSONString())
            .set(TopicDO::getType,requestParam.getType());
        // TODO 修改帖子需要通知粉丝？
        boolean update = update(wrapper);
        if (update)return null;
        else return "修改失败，请联系管理员!";
    }
    @Override
    public List<TopicPreviewRespDTO> listTopicByPage(int pageNumber, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + ":" +  type;
        String lockKey = "lock:" + key;
        // 从缓存中获取数据
        List<TopicPreviewRespDTO> list = cacheUtils.takeListFormCache(key, TopicPreviewRespDTO.class);
        if (list != null) return list;
        Page<TopicDO> page = new Page<>(pageNumber , 10);

        if (type == 0){
            baseMapper.selectPage(page, Wrappers.lambdaQuery(TopicDO.class)
                    .eq(TopicDO::getStatus, 1)
                .orderByDesc(TopicDO::getTime));
        }else{
            baseMapper.selectPage(page,Wrappers.lambdaQuery(TopicDO.class).eq(TopicDO::getType,type)
                .eq(TopicDO::getStatus, 1)
                .orderByDesc(TopicDO::getTime));
        }

        List<TopicDO> topics = page.getRecords();
        if (topics.isEmpty()) {
            // 将空值也存入缓存，避免缓存穿透
            cacheUtils.saveListToCache(key, new ArrayList<>(), 60);
            return new ArrayList<>();
        }
        list = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key , list, 60);

        return list;
    }

    @Override
    public List<TopicPreviewRespDTO> listTopicFollowByPage(int pageNumber, int id) {
        String key = Const.FORUM_TOPIC_FOLLOW_CACHE + ":" + id + pageNumber ;
        // 从缓存中获取数据
        List<TopicPreviewRespDTO> list = cacheUtils.takeListFormCache(key, TopicPreviewRespDTO.class);
        if (list != null) return list;

        Page<TopicDO> page = new Page<>(pageNumber , 10);
        // 读取自己邮箱
//        inboxTopicMapper.selectPage(page, Wrappers.lambdaQuery(InboxTopicDO.class)
//                .eq(InboxTopicDO::getUid,id)
//                .notIn(InboxTopicDO::getFid,id)
//                .orderByDesc(InboxTopicDO::getTime));
        Set<String> tidList = stringRedisTemplate.opsForZSet().range(Const.FEED_CACHE + id, 0, -1);
        Set<String> followList = stringRedisTemplate.opsForZSet().range(Const.FOLLOW_CACHE + id, 0, -1);
        if (followList == null || followList.isEmpty()){
            followList.forEach(uid -> {
                // 读取大V邮箱
                Set<String> range = stringRedisTemplate.opsForZSet().range(Const.FEED_BIG_CACHE + uid, 0, -1);
                if (range != null) {
                    if (tidList != null) {
                        tidList.addAll(range);
                    }
                }
            });
        }
        if (tidList.size() == 0 || tidList.isEmpty()){
            return null;
        }
        // 读取大V邮箱
        // List<InboxTopicDO> topics =  inboxTopicMapper.selectBigVTopic(id);
        // 与自身邮箱合并
//        topics.addAll(page.getRecords());
        topicMapper.selectPage(page, Wrappers.lambdaQuery(TopicDO.class)
            .in(TopicDO::getId, tidList)
            .eq(TopicDO::getStatus, 1)
            .orderByDesc(TopicDO::getTime));
        List<TopicDO> topics = page.getRecords();
//        topics = topics.stream().sorted(Comparator.comparing(InboxTopicDO::getTime).reversed()).toList();

        if (topics.isEmpty()){
            // 将空值也存入缓存，避免缓存穿透
            cacheUtils.saveListToCache(key, new ArrayList<>(), 60);
            return new ArrayList<>();
        }
        list = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key , list, 60);

        return list;
    }

    @Override
    public List<TopTopicRespDTO> listTopTopics() {
        List<TopicDO> topicDOS = baseMapper.selectList(Wrappers.<TopicDO>query().select("id","title","time")
            .eq("status", 1)
            .eq("top",1));
        return topicDOS.stream().map(topic -> {
            return BeanUtil.toBean(topic, TopTopicRespDTO.class);
        }).toList();
    }

    @Override
    public TopicDetailRespDTO getTopic(int tid, int uid) {
        // 查询文章信息
        TopicDO topic = baseMapper.selectById(tid);
        // 克隆到文章详细对象
        TopicDetailRespDTO topicDetailRespDTO = BeanUtil.toBean(topic,TopicDetailRespDTO.class);
        TopicDetailRespDTO.User user = new TopicDetailRespDTO.User();
        TopicDetailRespDTO.Interact interact = new TopicDetailRespDTO.Interact(
            hasInteract(tid,uid,"like"),
            hasInteract(tid,uid,"collect")
        );
        topicDetailRespDTO.setInteract(interact);
        topicDetailRespDTO.setUser(this.fillUserDetailByPrivacy(user,topic.getUid()));
        topicDetailRespDTO.setComments(topicCommentMapper.selectCount(Wrappers.<TopicCommentDO>query()
            .eq("tid",tid)
            .eq("root",-1)));
        return topicDetailRespDTO;
    }
    private boolean hasInteract(int tid, int uid, String type){
        String key = tid + ":" + uid;
        // 优化Redis读取逻辑，直接获取指定key的值而不是获取所有entries
        if (stringRedisTemplate.opsForHash().hasKey(type, key)) {
            Object value = stringRedisTemplate.opsForHash().get(type, key);
            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            }
        }
        return baseMapper.userInteractCount(tid,uid,type) > 0;
    }
    // 使用ConcurrentHashMap管理任务状态，更安全
    private final Map<String, Boolean> runningTasks = new ConcurrentHashMap<>();

    // 定时任务执行器
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

    @Override
    public void interact(Interact interact, boolean state) {
        try {
            // 1. 检查限流（保持原有逻辑）
            if (!flowUtils.limitPeriodCounterCheck("xzn:interact:ban"+interact.getUid(), 2, 1800)) {
                return;
            }

            // 2. 立即更新Redis（用于前端快速反馈）
            String key = interact.toKey();
            stringRedisTemplate.opsForHash().put(interact.getType(), key, Boolean.toString(state));

            // 3. 异步保存到数据库
            scheduleSaveToDatabase(interact, state);

            // 4. 如果是点赞/收藏操作，发送通知（异步）
            if (state) {
                CompletableFuture.runAsync(() -> sendNotification(interact));
            }

        } catch (Exception e) {
            log.error("点赞收藏操作失败 - uid:{}, tid:{}, type:{}, state:{}",
                interact.getUid(), interact.getTid(), interact.getType(), state, e);
        }
    }

    private void scheduleSaveToDatabase(Interact interact, boolean state) {
        String type = interact.getType();

        // 使用原子操作确保只有一个任务在执行
        if (runningTasks.putIfAbsent(type, Boolean.TRUE) == null) {
            scheduler.schedule(() -> {
                try {
                    saveToDatabase(type);
                } finally {
                    // 无论成功失败都要清理状态
                    runningTasks.remove(type);
                }
            }, 2, TimeUnit.SECONDS);
        }
        // 如果任务已在运行，新操作会被下一次调度处理
    }

    private void saveToDatabase(String type) {
        log.info("开始保存{}数据到数据库", type);

        try {
            // 获取Redis中的所有数据
            Map<Object, Object> redisData = stringRedisTemplate.opsForHash().entries(type);

            if (redisData.isEmpty()) {
                log.info("{}没有数据需要保存", type);
                return;
            }

            // 分离需要保存和删除的数据
            List<Interact> toSave = new ArrayList<>();
            List<Interact> toDelete = new ArrayList<>();

            redisData.forEach((k, v) -> {
                try {
                    Interact interact = Interact.parseInteract(k.toString(), type);
                    if (Boolean.parseBoolean(v.toString())) {
                        toSave.add(interact);
                    } else {
                        toDelete.add(interact);
                    }
                } catch (Exception e) {
                    log.error("解析数据失败: key={}, value={}", k, v, e);
                }
            });

            // 批量保存到数据库
            if (!toSave.isEmpty()) {
                baseMapper.addInteract(toSave, type);
                log.info("成功保存{}条{}数据", toSave.size(), type);
            }

            if (!toDelete.isEmpty()) {
                baseMapper.deleteInteract(toDelete, type);
                log.info("成功删除{}条{}数据", toDelete.size(), type);
            }

            // 清空Redis数据
            stringRedisTemplate.delete(type);
            log.info("{}数据保存完成，已清空Redis", type);

        } catch (Exception e) {
            log.error("保存{}数据到数据库失败", type, e);
            // 这里可以考虑将数据保存到本地文件或备用数据库进行补偿
            saveFailedDataToFile(type);
            throw e; // 重新抛出异常，让上层处理
        }
    }

    private void saveFailedDataToFile(String type) {
        try {
            Map<Object, Object> redisData = stringRedisTemplate.opsForHash().entries(type);
            String fileName = String.format("/tmp/interact_failed_%s_%d.txt", type, System.currentTimeMillis());

            try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
                redisData.forEach((k, v) -> {
                    writer.println(k + ":" + v);
                });
            }
            log.warn("失败数据已保存到文件: {}", fileName);
        } catch (Exception e) {
            log.error("保存失败数据到文件失败", e);
        }
    }

    private void sendNotification(Interact interact) {
        try {
            TopicDO topicDO = topicMapper.selectById(interact.getTid());
            if (topicDO == null) {
                log.warn("主题不存在: tid={}", interact.getTid());
                return;
            }

            AccountDO accountDO = accountMapper.selectById(interact.getUid());
            if (accountDO == null) {
                log.warn("用户不存在: uid={}", interact.getUid());
                return;
            }

            String title, message;
            if ("collect".equals(interact.getType())) {
                title = "您的帖子有新的收藏";
                message = accountDO.getUsername() + "收藏了你发表的主题: " + topicDO.getTitle() + "，快去看看吧!";
            } else {
                title = "您的帖子有新的点赞";
                message = accountDO.getUsername() + "点赞了你发表的主题: " + topicDO.getTitle() + "，快去看看吧!";
            }

            notificationService.addNotification(
                topicDO.getUid(),
                title,
                message,
                "success",
                "/index/topic-detail/" + interact.getTid()
            );

        } catch (Exception e) {
            log.error("发送通知失败", e);
        }
    }

    // 添加手动补偿方法，用于修复数据不一致问题
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    public void compensateData() {
        log.info("开始数据补偿检查");

        for (String type : Arrays.asList("like", "collect")) {
            try {
                compensateDataForType(type);
            } catch (Exception e) {
                log.error("补偿{}数据失败", type, e);
            }
        }
    }

    private void compensateDataForType(String type) {
        // 1. 获取Redis中的数据
        Map<Object, Object> redisData = stringRedisTemplate.opsForHash().entries(type);

        if (redisData.isEmpty()) {
            return;
        }

        // 2. 检查数据库中是否存在这些记录
        for (Map.Entry<Object, Object> entry : redisData.entrySet()) {
            try {
                Interact interact = Interact.parseInteract(entry.getKey().toString(), type);
                boolean shouldExist = Boolean.parseBoolean(entry.getValue().toString());

                boolean existsInDb = baseMapper.userInteractCount(interact.getTid(), interact.getUid(), type) > 0;

                // 如果Redis中标记为存在，但数据库中不存在，则插入
                if (shouldExist && !existsInDb) {
                    log.info("补偿数据: 插入{}记录 tid={}, uid={}", type, interact.getTid(), interact.getUid());
                    baseMapper.addInteract(Collections.singletonList(interact), type);
                }
                // 如果Redis中标记为不存在，但数据库中存在，则删除
                else if (!shouldExist && existsInDb) {
                    log.info("补偿数据: 删除{}记录 tid={}, uid={}", type, interact.getTid(), interact.getUid());
                    baseMapper.deleteInteract(Collections.singletonList(interact), type);
                }

            } catch (Exception e) {
                log.error("补偿数据处理失败: key={}", entry.getKey(), e);
            }
        }
    }


    @Override
    public List<TopicCollectRespDTO> getCollects(int id) {
        List<TopicDO> topicDOS = baseMapper.collectTopics(id);
        return BeanUtil.copyToList(topicDOS,TopicCollectRespDTO.class);
    }

    /**
     * @param requestParam 评论内容
     * @param uid 评论用户id
     */
    @Override
    public String addComment(int uid, AddCommentReqDTO requestParam) {
        String key = Const.FORUM_TOPIC_COMMENT_COUNTER + uid;
        // 检验内容
        if (!textLimitCheck(JSONObject.parseObject(requestParam.getContent()),2000)){
            return "评论内容太多，发表失败！";
        }
        // 检验发文频繁
        if (!flowUtils.limitPeriodCounterCheck(key,2,60)){
            return "发表评论频繁，请稍后再试！";
        }
        TopicCommentDO bean = BeanUtil.toBean(requestParam, TopicCommentDO.class);
        bean.setUid(uid);
        bean.setTime(new Date());
        topicCommentMapper.insert(bean);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("req",  JSONObject.toJSONString(requestParam));
        hashMap.put("bean",JSONObject.toJSONString(bean));
        // 利用消息队列发送
        rabbitTemplate.convertAndSend("notificationComment",hashMap);
        return null;
    }

    @Override
    public String deleteComment(int id, int cid) {
        TopicCommentDO topicCommentDO = topicCommentMapper.selectById(cid);
        if (!topicCommentDO.getUid().equals(id)){
            return "删除评论错误，请联系管理员！";
        }
        topicCommentMapper.deleteById(cid);
        return null;
    }

    @Override
    public List<CommentRespDTO> comments(int tid, int pageNumber) {
        Page<TopicCommentDO> page = Page.of(pageNumber,10);
        LambdaQueryWrapper<TopicCommentDO> queryWrapper = Wrappers.lambdaQuery(TopicCommentDO.class)
            .eq(TopicCommentDO::getTid, tid)
            .eq(TopicCommentDO::getRoot,-1);
        topicCommentMapper.selectPage(page, queryWrapper);
        List<CommentRespDTO> comments= toCommentList(page.getRecords());

        //查询所有根评论对应的子评论 并把子评论赋值给对应的属性
        for (CommentRespDTO dto : comments){
            List<CommentRespDTO> children = getChildren(dto.getId());
            dto.setChildren(children);
        }
        return comments;
    }

    @Override
    public String deleteDraft(int id, int uid) {
        LambdaQueryWrapper<TopicDO> wrapper = Wrappers.lambdaQuery(TopicDO.class)
            .eq(TopicDO::getId, id)
            .eq(TopicDO::getUid, uid)
            .eq(TopicDO::getStatus, 0); // 确保只删除草稿

        int update = topicMapper.delete(wrapper);
        if (update > 0) return null;
        else return "删除失败，请联系管理员!";
    }

    @Override
    public Page<TopicDO> listDraftsByUid(int uid, int page,  int size) {
        Page<TopicDO> pageDO = Page.of(page,size);
        LambdaQueryWrapper<TopicDO> queryWrapper = Wrappers.lambdaQuery(TopicDO.class)
            .eq(TopicDO::getUid, uid)
            .eq(TopicDO::getStatus, 0)
            .orderByDesc(TopicDO::getTime); // 按时间升序排序
        topicMapper.selectPage(pageDO, queryWrapper);
        return pageDO;
    }

    private List<CommentRespDTO> getChildren(int cid){
        LambdaQueryWrapper<TopicCommentDO> queryWrapper = Wrappers.lambdaQuery(TopicCommentDO.class)
            .eq(TopicCommentDO::getRoot,cid)
            .orderByAsc(TopicCommentDO::getTime);
        List<TopicCommentDO> list = topicCommentMapper.selectList(queryWrapper);
        return toCommentList(list);
    }
    private List<CommentRespDTO> toCommentList(List<TopicCommentDO> list) {
        return list.stream().map(dto -> {
            CommentRespDTO bean = BeanUtil.toBean(dto, CommentRespDTO.class);
            if (dto.getQuote() > 0){
                AccountDO accountDO = accountMapper.selectById(dto.getQuote());
                bean.setQuoteName(accountDO.getUsername());
            }
            User user = new User();
            this.fillUserDetailByPrivacy(user,dto.getUid());
            bean.setUser(user);
            return bean;
        }).toList();
    }
    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object> imageHandler){
        for(Object op : ops){
            Object insert = JSONObject.from(op).get("insert");
            // 如果是String 就是普通句子用text存储
            if (insert instanceof String text){
                if (previewText.length() >= 300)continue;;
                previewText.append(text);
            }else if (insert instanceof Map<?,?> map){
                // 图片则用list存储
                Optional.ofNullable(map.get("image")).ifPresent(imageHandler);
            }
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
        this.shortContent(ops,previewText,obj->images.add(obj.toString()));
        bean.setText(previewText.length() > 300 ? previewText.substring(0, 300) :  previewText.toString());
        bean.setImages(images);
        return bean;
    }
    // 解析帖子
    private TopicPreviewRespDTO resolveToPreviewFollow(InboxTopicDO topicDO){
        // 获取帖子
        TopicPreviewRespDTO bean = new TopicPreviewRespDTO();
        // 得到user属性
        BeanUtils.copyProperties(accountMapper.selectById(topicDO.getFid()),bean);
        // 得到帖子属性
        BeanUtils.copyProperties(topicDO, bean);
        bean.setLike(baseMapper.interactCount(topicDO.getTid(),"like"));
        bean.setCollect(baseMapper.interactCount(topicDO.getTid(),"collect"));
        // 获取点赞收藏
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topicDO.getContent()).getJSONArray("ops");
        this.shortContent(ops,previewText,obj->images.add(obj.toString()));
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