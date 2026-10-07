package com.weatherapp.dto;

import jakarta.validation.constraints.*;

public record FavoriteRequest(
        @NotBlank(message = "City name is required") @Size(max = 100, message = "City name is too long") String cityName,
        @Size(max = 100) String country,
        @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
