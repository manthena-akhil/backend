package com.weatherapp.service;

import com.weatherapp.dto.FavoriteDto;
import com.weatherapp.dto.FavoriteRequest;
import com.weatherapp.entity.FavoriteCity;
import com.weatherapp.entity.User;
import com.weatherapp.exception.DuplicateResourceException;
import com.weatherapp.exception.ResourceNotFoundException;
import com.weatherapp.repository.FavoriteCityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteCityRepository repo;
    private final UserService userService;

    public FavoriteService(FavoriteCityRepository repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    static FavoriteDto toDto(FavoriteCity f) {
        return new FavoriteDto(f.getId(), f.getCityName(), f.getCountry(), f.getLatitude(), f.getLongitude(), f.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<FavoriteDto> list(String email) {
        User u = userService.getByEmail(email);
        return repo.findByUserIdOrderByCreatedAtDesc(u.getId()).stream().map(FavoriteService::toDto).toList();
    }

    @Transactional
    public FavoriteDto add(String email, FavoriteRequest req) {
        User u = userService.getByEmail(email);
        String city = req.cityName().trim();
        if (repo.existsByUserIdAndCityNameIgnoreCase(u.getId(), city)) {
            throw new DuplicateResourceException("This city is already in your favorites");
        }
        FavoriteCity f = new FavoriteCity();
        f.setUser(u);
        f.setCityName(city);
        f.setCountry(req.country());
        f.setLatitude(req.latitude());
        f.setLongitude(req.longitude());
        return toDto(repo.save(f));
    }

    @Transactional
    public void delete(String email, Long id) {
        User u = userService.getByEmail(email);
        FavoriteCity f = repo.findByIdAndUserId(id, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Favorite city not found"));
        repo.delete(f);
    }
}
