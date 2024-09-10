package com.example.entity.dao;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class WeatherDO {
    JSONObject location;
    JSONObject now;
    JSONArray hourly;

}
