package com.weatherapp.service;

import com.weatherapp.dto.*;
import com.weatherapp.entity.User;
import com.weatherapp.exception.*;
import com.weatherapp.repository.UserRepository;
import com.weatherapp.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock JwtService jwt;
    PasswordEncoder encoder = new BCryptPasswordEncoder();

    AuthService service() { return new AuthService(users, encoder, jwt); }

    @Test
    void registerHashesPasswordAndReturnsToken() {
        when(users.existsByEmail("kiran@example.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwt.generateToken("kiran@example.com")).thenReturn("token");

        AuthResponse res = service().register(new RegisterRequest("Kiran", "Kiran@Example.com", "Passw0rd1", "Passw0rd1"));

        assertEquals("token", res.token());
        assertEquals("kiran@example.com", res.user().email());
        verify(users).save(argThat(u -> !u.getPassword().equals("Passw0rd1") && u.getPassword().startsWith("$2")));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(users.existsByEmail("a@b.com")).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class,
                () -> service().register(new RegisterRequest("A", "a@b.com", "Passw0rd1", "Passw0rd1")));
    }

    @Test
    void registerRejectsPasswordMismatch() {
        assertThrows(ValidationException.class,
                () -> service().register(new RegisterRequest("A", "a@b.com", "Passw0rd1", "Different1")));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        User u = new User();
        u.setName("A"); u.setEmail("a@b.com"); u.setPassword(encoder.encode("Passw0rd1"));
        when(users.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(jwt.generateToken("a@b.com")).thenReturn("jwt");
        assertEquals("jwt", service().login(new LoginRequest("a@b.com", "Passw0rd1")).token());
    }

    @Test
    void loginFailsWithWrongPasswordOrUnknownUser() {
        User u = new User();
        u.setEmail("a@b.com"); u.setPassword(encoder.encode("Passw0rd1"));
        when(users.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(users.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> service().login(new LoginRequest("a@b.com", "wrong")));
        assertThrows(InvalidCredentialsException.class, () -> service().login(new LoginRequest("x@y.com", "whatever1")));
    }
}
