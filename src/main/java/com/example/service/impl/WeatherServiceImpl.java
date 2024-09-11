package com.example.service.impl;

import static com.example.utils.Const.FORUM_WEATHER_CACHE;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.entity.dao.WeatherDO;
import com.example.service.WeatherService;
import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author Nruonan
 * @description
 */
@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    RestTemplate restTemplate;

    @Value("${spring.weather.key}")
    String key;
    @Override
    public WeatherDO fetchWeather(double longitude, double latitude){
        return fetchWeatherCache(longitude,latitude);
    }

    private WeatherDO fetchWeatherCache(double longitude, double latitude){
        // 首先定位获取位置信息
        JSONObject geo = this.decompressStringToJson(restTemplate.getForObject(
            "https://geoapi.qweather.com/v2/city/lookup?location=" + longitude + "," + latitude + "&key=" + key,
            byte[].class));
        if (geo == null)return null;
        // 获取位置信息中的id
        JSONObject location = geo.getJSONArray("location").getJSONObject(0);
        Integer id = location.getInteger("id");

        String key = FORUM_WEATHER_CACHE+id;
        // 读取天气信息 如果有就可以直接返回
        String cache = stringRedisTemplate.opsForValue().get(key);
        if (cache != null){
            return JSONObject.parseObject(cache).to(WeatherDO.class);
        }
        // 获取天气信息
        WeatherDO weatherDO = this.fetchFromAPI(id, location);
        if (weatherDO == null)return null;
        // 缓存天气信息
        stringRedisTemplate.opsForValue().set(key,JSONObject.from(weatherDO).toJSONString(),1, TimeUnit.HOURS);
        return weatherDO;
    }
    private WeatherDO fetchFromAPI(int id,JSONObject location){
        WeatherDO weatherDO = new WeatherDO();
        // 存入位置
        weatherDO.setLocation(location);
        JSONObject now = this.decompressStringToJson(restTemplate.getForObject(
            "https://devapi.qweather.com/v7/weather/now?location=" + id + "&key=" + key,
            byte[].class));
        if (now == null)return  null;
        weatherDO.setNow(now.getJSONObject("now"));
        JSONObject hourly = this.decompressStringToJson(restTemplate.getForObject(
            "https://devapi.qweather.com/v7/weather/24h?location=" + id + "&key=" + key,
            byte[].class));
        if (hourly == null)return null;
        weatherDO.setHourly(new JSONArray(hourly.getJSONArray("hourly").stream().limit(5).toList()));
        return weatherDO;
    }
    private JSONObject decompressStringToJson(byte[] data){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data));
            byte[] buffer = new byte[1024];
            int read;
            while ((read = gzip.read(buffer)) != -1)
                stream.write(buffer, 0, read);
            gzip.close();
            stream.close();
            return JSONObject.parseObject(stream.toString());
        }catch (IOException e) {
            return null;
        }
    }
}
