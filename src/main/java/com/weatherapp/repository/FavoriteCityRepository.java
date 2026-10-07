package com.weatherapp.repository;

import com.weatherapp.entity.FavoriteCity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteCityRepository extends JpaRepository<FavoriteCity, Long> {
    List<FavoriteCity> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndCityNameIgnoreCase(Long userId, String cityName);
    Optional<FavoriteCity> findByIdAndUserId(Long id, Long userId);
}
