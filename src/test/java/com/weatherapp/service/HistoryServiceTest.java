package com.weatherapp.service;

import com.weatherapp.dto.WeatherDto;
import com.weatherapp.entity.User;
import com.weatherapp.entity.WeatherHistory;
import com.weatherapp.exception.ResourceNotFoundException;
import com.weatherapp.repository.WeatherHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {
    @Mock WeatherHistoryRepository repo;
    @Mock UserService userService;

    private User user() {
        User u = mock(User.class);
        when(u.getId()).thenReturn(1L);
        when(userService.getByEmail("a@b.com")).thenReturn(u);
        return u;
    }

    @Test
    void recordStoresWeatherSnapshot() {
        user();
        WeatherDto w = new WeatherDto("Hyderabad", "India", 17.4, 78.5, 29, 31, 24, 32, 65, 12, 1012, 8,
                "Clear Sky", "01d", "06:10 AM", "06:15 PM", "now", null);
        new HistoryService(repo, userService).record("a@b.com", w);
        verify(repo).save(argThat((WeatherHistory h) -> h.getCityName().equals("Hyderabad")
                && h.getTemperature() == 29 && h.getHumidity() == 65 && h.getWeatherCondition().equals("Clear Sky")));
    }

    @Test
    void listReturnsMappedRecords() {
        user();
        WeatherHistory h = new WeatherHistory();
        h.setCityName("Delhi");
        when(repo.findTop100ByUserIdOrderBySearchedAtDesc(1L)).thenReturn(List.of(h));
        assertEquals("Delhi", new HistoryService(repo, userService).list("a@b.com").get(0).cityName());
    }

    @Test
    void deleteMissingRecordFails() {
        user();
        when(repo.findByIdAndUserId(3L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> new HistoryService(repo, userService).delete("a@b.com", 3L));
    }

    @Test
    void clearDeletesAllForUser() {
        user();
        new HistoryService(repo, userService).clear("a@b.com");
        verify(repo).deleteByUserId(1L);
    }
}
