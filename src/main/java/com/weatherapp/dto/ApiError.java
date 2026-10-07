package com.weatherapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(boolean success, String message, LocalDateTime timestamp, Map<String, String> errors) {
    public static ApiError of(String message) { return new ApiError(false, message, LocalDateTime.now(), null); }
    public static ApiError of(String message, Map<String, String> errors) { return new ApiError(false, message, LocalDateTime.now(), errors); }
}
