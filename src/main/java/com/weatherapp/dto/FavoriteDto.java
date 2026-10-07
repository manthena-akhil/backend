package com.weatherapp.dto;

import java.time.LocalDateTime;

public record FavoriteDto(Long id, String cityName, String country, Double latitude, Double longitude, LocalDateTime createdAt) {}
