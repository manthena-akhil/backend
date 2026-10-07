package com.weatherapp.service;

import com.weatherapp.dto.ProfileDto;
import com.weatherapp.dto.ProfileUpdateRequest;
import com.weatherapp.entity.User;
import com.weatherapp.exception.ResourceNotFoundException;
import com.weatherapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User getByEmail(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public static ProfileDto toDto(User u) { return new ProfileDto(u.getId(), u.getName(), u.getEmail(), u.getCreatedAt()); }

    public ProfileDto profile(String email) { return toDto(getByEmail(email)); }

    @Transactional
    public ProfileDto update(String email, ProfileUpdateRequest req) {
        User u = getByEmail(email);
        u.setName(req.name().trim());
        if (req.password() != null && !req.password().isBlank()) u.setPassword(encoder.encode(req.password()));
        return toDto(users.save(u));
    }
}
