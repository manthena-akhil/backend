package com.weatherapp.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Full name is required") @Size(max = 100, message = "Name is too long") String name,
        @NotBlank(message = "Email is required") @Email(message = "Please enter a valid email address") @Size(max = 150) String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Password must contain at least one letter and one number")
        String password,
        @NotBlank(message = "Please confirm your password") String confirmPassword) {}
