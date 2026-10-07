package com.weatherapp.dto;

public record HourlyDto(String time, String icon, String condition, double temperature,
                        int precipitationProbability, double windSpeed, int humidity) {}
