package com.example.utils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 * @description
 */
@Component
public class CacheUtils {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    public <T> T takeFromCache(String key, Class<T> dataType) {
        String s = stringRedisTemplate.opsForValue().get(key);
        if(s == null) return null;
        return JSONObject.parseObject(s).to(dataType);
    }
    public <T> List<T> takeListFormCache(String key, Class<T> itemType){
        String r = stringRedisTemplate.opsForValue().get(key);
        if (r == null)return null;
        return JSONArray.parseArray(r).toList(itemType);
    }
    public <T> void saveListToCache(String key, List<T> list , long expire){
        stringRedisTemplate.opsForValue().set(key, JSONArray.from(list).toJSONString(),expire, TimeUnit.SECONDS);
    }
    public <T> void saveToCache(String key, T data, long expire) {
        stringRedisTemplate.opsForValue().set(key, JSONObject.from(data).toJSONString(), expire, TimeUnit.SECONDS);
    }
    public void deleteCachePattern(String key){
        Set<String> keys = Optional.ofNullable(stringRedisTemplate.keys(key)).orElse(Collections.emptySet());
        stringRedisTemplate.delete(keys);
    }

    public void deleteCache(String key){
        stringRedisTemplate.delete(key);
    }
}
