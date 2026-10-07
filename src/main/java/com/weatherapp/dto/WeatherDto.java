package com.weatherapp.dto;

/** Current weather. Temperatures are Celsius, wind is km/h, visibility is km, pressure is hPa. */
public record WeatherDto(
        String city, String country, double latitude, double longitude,
        double temperature, double feelsLike, double minTemperature, double maxTemperature,
        int humidity, double windSpeed, int pressure, double visibility,
        String condition, String icon, String sunrise, String sunset, String localTime,
        Double uvIndex) {}
