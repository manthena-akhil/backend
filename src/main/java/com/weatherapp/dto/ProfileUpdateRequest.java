package com.weatherapp.dto;

import jakarta.validation.constraints.*;

public record ProfileUpdateRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Name is too long") String name,
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
        String password) {}
