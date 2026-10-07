package com.weatherapp.dto;

public record ForecastDayDto(String day, String date, String icon, String condition,
                             double maxTemperature, double minTemperature, int humidity, double windSpeed) {}
