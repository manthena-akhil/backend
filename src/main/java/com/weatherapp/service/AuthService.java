package com.weatherapp.service;

import com.weatherapp.dto.*;
import com.weatherapp.entity.User;
import com.weatherapp.exception.InvalidCredentialsException;
import com.weatherapp.exception.UserAlreadyExistsException;
import com.weatherapp.exception.ValidationException;
import com.weatherapp.repository.UserRepository;
import com.weatherapp.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) throw new ValidationException("Passwords do not match");
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new UserAlreadyExistsException("An account with this email already exists");
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u = users.save(u);
        return new AuthResponse(jwt.generateToken(u.getEmail()), UserService.toDto(u));
    }

    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!encoder.matches(req.password(), u.getPassword())) throw new InvalidCredentialsException("Invalid email or password");
        return new AuthResponse(jwt.generateToken(u.getEmail()), UserService.toDto(u));
    }
}
