package com.example.controller;



import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;


import com.example.entity.RestBean;
import com.example.entity.dao.NewDO;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController()
@RequestMapping("/api")
public class NewController {
    @Value("${spring.new.url}")
    private String apiUrl;

    @Value("${spring.new.key}")
    private String key;
    @Resource
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/new")
    public RestBean< ArrayList<NewDO>> getNewList() throws IOException {
        ArrayList<NewDO> list = new ArrayList<>();
        // 从 Redis 获取数据，如果存在则直接返回
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(Const.FORUM_NEW_CACHE))) {
            String cachedData = stringRedisTemplate.opsForValue().get(Const.FORUM_NEW_CACHE);
            System.out.println(cachedData);
            // 将 Redis 中的 JSON 字符串转换为 ArrayList<NewDO>
            JSONArray cachedArray = JSONUtil.parseArray(cachedData);
            for (int i = 0; i < cachedArray.size(); i++) {
                JSONObject newJson = cachedArray.getJSONObject(i);
                list.add(new NewDO(newJson.getStr("title"), newJson.getStr("url")));
            }
            return RestBean.success(list);
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("key", key);
        map.put("type", "top");
        map.put("page", "1");
        map.put("page_size", "10");
        map.put("is_filter", "1");

        URL url = new URL(String.format(apiUrl + "?" + params(map)));
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");

        // 获取响应
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();


        // 使用 Gson 解析 JSON 响应
        JSONObject jsonObject = JSONUtil.parseObj(response.toString());
        JSONArray newJsonArray = jsonObject.getJSONObject("result").getJSONArray("data");


        for(int i = 0; i < newJsonArray.size(); i++){
            JSONObject newJson = newJsonArray.getJSONObject(i);

            list.add(new NewDO(newJson.getStr("title"),newJson.getStr("url")));

        }
        // 将数据存入 Redis，并设置过期时间（例如 1 小时）
        // 将 List 转换为 JSON 字符串并存入 Redis
        stringRedisTemplate.opsForValue().set(Const.FORUM_NEW_CACHE, JSONUtil.toJsonStr(newJsonArray), 4, TimeUnit.HOURS);
        return RestBean.success(list);
    }

    public static String params(Map<String, String> map) {
        return map.entrySet().stream()
            .map(entry -> {
                try {
                    return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    return entry.getKey() + "=" + entry.getValue();
                }
            })
            .collect(Collectors.joining("&"));
    }
}
