package com.weatherapp.dto;

import java.time.LocalDateTime;

public record HistoryDto(Long id, String cityName, String country, Double temperature, Double feelsLike,
                         Integer humidity, Double windSpeed, Integer pressure, Double visibility,
                         String weatherCondition, LocalDateTime searchedAt) {}
