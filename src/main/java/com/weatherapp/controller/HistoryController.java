package com.weatherapp.controller;

import com.weatherapp.dto.HistoryDto;
import com.weatherapp.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryService history;

    public HistoryController(HistoryService history) { this.history = history; }

    @GetMapping
    public List<HistoryDto> list(Authentication auth) { return history.list(auth.getName()); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        history.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(Authentication auth) {
        history.clear(auth.getName());
        return ResponseEntity.noContent().build();
    }
}
