package com.weatherapp.controller;

import com.weatherapp.dto.ProfileDto;
import com.weatherapp.dto.ProfileUpdateRequest;
import com.weatherapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @GetMapping("/profile")
    public ProfileDto profile(Authentication auth) { return users.profile(auth.getName()); }

    @PutMapping("/profile")
    public ProfileDto update(@Valid @RequestBody ProfileUpdateRequest req, Authentication auth) {
        return users.update(auth.getName(), req);
    }
}
