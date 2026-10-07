package com.weatherapp.service;

import com.weatherapp.dto.HistoryDto;
import com.weatherapp.dto.WeatherDto;
import com.weatherapp.entity.User;
import com.weatherapp.entity.WeatherHistory;
import com.weatherapp.exception.ResourceNotFoundException;
import com.weatherapp.repository.WeatherHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HistoryService {
    private final WeatherHistoryRepository repo;
    private final UserService userService;

    public HistoryService(WeatherHistoryRepository repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    static HistoryDto toDto(WeatherHistory h) {
        return new HistoryDto(h.getId(), h.getCityName(), h.getCountry(), h.getTemperature(), h.getFeelsLike(),
                h.getHumidity(), h.getWindSpeed(), h.getPressure(), h.getVisibility(), h.getWeatherCondition(), h.getSearchedAt());
    }

    @Transactional(readOnly = true)
    public List<HistoryDto> list(String email) {
        User u = userService.getByEmail(email);
        return repo.findTop100ByUserIdOrderBySearchedAtDesc(u.getId()).stream().map(HistoryService::toDto).toList();
    }

    @Transactional
    public void record(String email, WeatherDto w) {
        User u = userService.getByEmail(email);
        WeatherHistory h = new WeatherHistory();
        h.setUser(u);
        h.setCityName(w.city());
        h.setCountry(w.country());
        h.setTemperature(w.temperature());
        h.setFeelsLike(w.feelsLike());
        h.setHumidity(w.humidity());
        h.setWindSpeed(w.windSpeed());
        h.setPressure(w.pressure());
        h.setVisibility(w.visibility());
        h.setWeatherCondition(w.condition());
        repo.save(h);
    }

    @Transactional
    public void delete(String email, Long id) {
        User u = userService.getByEmail(email);
        WeatherHistory h = repo.findByIdAndUserId(id, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("History record not found"));
        repo.delete(h);
    }

    @Transactional
    public void clear(String email) {
        repo.deleteByUserId(userService.getByEmail(email).getId());
    }
}
