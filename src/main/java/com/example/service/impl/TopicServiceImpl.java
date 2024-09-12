package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dao.TopicDO;
import com.example.entity.dao.TopicTypeDO;
import com.example.entity.dto.req.TopicCreateReqDTO;
import com.example.entity.dto.resp.TopicPreviewRespDTO;
import com.example.entity.dto.resp.TopicTypeRespDTO;
import com.example.mapper.TopicMapper;
import com.example.mapper.TopicTypeMapper;
import com.example.service.TopicService;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import com.example.utils.FlowUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * @author Nruonan
 * @description
 */
@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, TopicDO> implements TopicService {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtils flowUtils;

    @Resource
    CacheUtils cacheUtils;

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
            cacheUtils.deleteCache(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            return null;
        }else{
            return "内部错误，请联系管理员!";
        }

    }

    @Override
    public List<TopicPreviewRespDTO> listTopicByPage(int page, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + page + ":" +  type;
        List<TopicPreviewRespDTO> list = cacheUtils.takeListFormCache(key, TopicPreviewRespDTO.class);
        if (list != null) return list;
        List<TopicDO> topics;
        if (type == 0){
            // 全查
            topics = baseMapper.topicList(page * 10);
        }else{
            topics = baseMapper.topicListByType(page * 10, type);
        }
        if (topics.isEmpty())return null;
        list = topics.stream().map(this::resolveToPreview).toList();
        cacheUtils.saveListToCache(key , list, 60);
        return list;
    }

    private TopicPreviewRespDTO resolveToPreview(TopicDO topicDO){
        TopicPreviewRespDTO bean = BeanUtil.toBean(topicDO, TopicPreviewRespDTO.class);
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
