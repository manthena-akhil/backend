package com.weatherapp.service;

import com.weatherapp.dto.FavoriteRequest;
import com.weatherapp.entity.FavoriteCity;
import com.weatherapp.entity.User;
import com.weatherapp.exception.DuplicateResourceException;
import com.weatherapp.exception.ResourceNotFoundException;
import com.weatherapp.repository.FavoriteCityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {
    @Mock FavoriteCityRepository repo;
    @Mock UserService userService;

    private User user() {
        User u = mock(User.class);
        when(u.getId()).thenReturn(1L);
        when(userService.getByEmail("a@b.com")).thenReturn(u);
        return u;
    }

    @Test
    void addCreatesFavorite() {
        user();
        when(repo.existsByUserIdAndCityNameIgnoreCase(1L, "Hyderabad")).thenReturn(false);
        when(repo.save(any(FavoriteCity.class))).thenAnswer(i -> i.getArgument(0));
        var dto = new FavoriteService(repo, userService).add("a@b.com", new FavoriteRequest(" Hyderabad ", "India", 17.38, 78.48));
        assertEquals("Hyderabad", dto.cityName());
        assertEquals("India", dto.country());
    }

    @Test
    void addRejectsDuplicate() {
        user();
        when(repo.existsByUserIdAndCityNameIgnoreCase(1L, "Delhi")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> new FavoriteService(repo, userService).add("a@b.com", new FavoriteRequest("Delhi", "India", null, null)));
        verify(repo, never()).save(any());
    }

    @Test
    void deleteRemovesOwnFavorite() {
        user();
        FavoriteCity f = new FavoriteCity();
        when(repo.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(f));
        new FavoriteService(repo, userService).delete("a@b.com", 5L);
        verify(repo).delete(f);
    }

    @Test
    void deleteUnknownOrForeignFavoriteFails() {
        user();
        when(repo.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> new FavoriteService(repo, userService).delete("a@b.com", 9L));
    }
}
