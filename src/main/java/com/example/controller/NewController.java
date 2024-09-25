package com.example.controller;





import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.RestBean;
import com.example.entity.dao.NewDO;
import com.example.utils.CacheUtils;
import com.example.utils.Const;
import io.swagger.v3.core.util.Json;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.JsonParser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Nruonan
 * @description
 */
@RestController()
@Slf4j
@RequestMapping("/api")
public class NewController {
    @Resource
    private CacheUtils cacheUtils;

    final String[] urlNames = new String[]{"https://tenapi.cn/v2/baiduhot","https://tenapi.cn/v2/douyinhot","https://tenapi.cn/v2/weibohot",
    "https://tenapi.cn/v2/zhihuhot","https://tenapi.cn/v2/bilihot","https://tenapi.cn/v2/toutiaohot"};

    @GetMapping("/new")
    public RestBean<List<List<NewDO>>> anew() throws Exception {
        List<List<NewDO>> list = new ArrayList<>();
        for (String urlName : urlNames){
            list.add(fetchHot(urlName));
        }
        return RestBean.success(list);
    }




    public  List<NewDO> fetchHot(String urlName) throws Exception {

        String com = urlName.substring(21,urlName.length() - 3);
        List<NewDO> newDOs = cacheUtils.takeListFormCache(Const.FORUM_HOT_CACHE + com, NewDO.class);
        if (newDOs != null)return newDOs;
        URL url = new URL(urlName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            newDOs = parseResponse(response.toString(), com);
        } else {
            log.info("获取热榜数据出错，请搜索ten-api解决问题!");
        }
        conn.disconnect();
        return newDOs;
    }

    public  List<NewDO> parseResponse(String response,String title) {
        JSONObject jsonObject = JSONObject.parseObject(response);
        List<NewDO> list = new ArrayList<>();
        JSONArray hotList = jsonObject.getJSONArray("data");
        for (int i = 0; i < Math.min(hotList.size(), 15); i++) {
            JSONObject item = hotList.getJSONObject(i);
            String name = item.getString("name");
            String url = item.getString("url");
            list.add(new NewDO(name,url));
        }
        cacheUtils.saveListToCache(Const.FORUM_HOT_CACHE + title, list,10800);
        return list;
    }


}
