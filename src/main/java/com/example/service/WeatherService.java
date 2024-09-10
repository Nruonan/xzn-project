package com.example.service;

import com.example.entity.dao.WeatherDO;

/**
 * @author Nruonan
 * @description
 */
public interface WeatherService {
    WeatherDO fetchWeather(double longitude, double latitude);
}
