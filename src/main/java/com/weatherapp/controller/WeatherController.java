package com.weatherapp.controller;

import com.weatherapp.dto.ForecastDayDto;
import com.weatherapp.dto.HourlyDto;
import com.weatherapp.dto.WeatherDto;
import com.weatherapp.service.HistoryService;
import com.weatherapp.service.WeatherService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {
    private final WeatherService weather;
    private final HistoryService history;

    public WeatherController(WeatherService weather, HistoryService history) {
        this.weather = weather;
        this.history = history;
    }

    @GetMapping("/city/{city}")
    public WeatherDto byCity(@PathVariable String city, Authentication auth) {
        WeatherDto w = weather.currentByCity(city);
        history.record(auth.getName(), w);
        return w;
    }

    @GetMapping("/coordinates")
    public WeatherDto byCoordinates(@RequestParam double lat, @RequestParam double lon, Authentication auth) {
        WeatherDto w = weather.currentByCoordinates(lat, lon);
        history.record(auth.getName(), w);
        return w;
    }

    @GetMapping("/forecast/{city}")
    public List<ForecastDayDto> forecast(@PathVariable String city) { return weather.dailyForecast(city); }

    @GetMapping("/hourly/{city}")
    public List<HourlyDto> hourly(@PathVariable String city) { return weather.hourlyForecast(city); }
}
