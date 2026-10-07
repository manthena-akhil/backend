package com.weatherapp.controller;

import com.weatherapp.dto.FavoriteDto;
import com.weatherapp.dto.FavoriteRequest;
import com.weatherapp.service.FavoriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {
    private final FavoriteService favorites;

    public FavoriteController(FavoriteService favorites) { this.favorites = favorites; }

    @GetMapping
    public List<FavoriteDto> list(Authentication auth) { return favorites.list(auth.getName()); }

    @PostMapping
    public ResponseEntity<FavoriteDto> add(@Valid @RequestBody FavoriteRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favorites.add(auth.getName(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        favorites.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
