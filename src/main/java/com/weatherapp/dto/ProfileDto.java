package com.weatherapp.dto;

import java.time.LocalDateTime;

public record ProfileDto(Long id, String name, String email, LocalDateTime createdAt) {}
