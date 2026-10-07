package com.weatherapp.repository;

import com.weatherapp.entity.WeatherHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WeatherHistoryRepository extends JpaRepository<WeatherHistory, Long> {
    List<WeatherHistory> findTop100ByUserIdOrderBySearchedAtDesc(Long userId);
    Optional<WeatherHistory> findByIdAndUserId(Long id, Long userId);
    void deleteByUserId(Long userId);
}
