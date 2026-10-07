package com.weatherapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_history")
public class WeatherHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;
    @Column(length = 100)
    private String country;
    private Double temperature;
    @Column(name = "feels_like")
    private Double feelsLike;
    private Integer humidity;
    @Column(name = "wind_speed")
    private Double windSpeed;
    private Integer pressure;
    private Double visibility;
    @Column(name = "weather_condition", length = 100)
    private String weatherCondition;
    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    @PrePersist
    void onCreate() { if (searchedAt == null) searchedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getCityName() { return cityName; }
    public void setCityName(String v) { this.cityName = v; }
    public String getCountry() { return country; }
    public void setCountry(String v) { this.country = v; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double v) { this.temperature = v; }
    public Double getFeelsLike() { return feelsLike; }
    public void setFeelsLike(Double v) { this.feelsLike = v; }
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer v) { this.humidity = v; }
    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double v) { this.windSpeed = v; }
    public Integer getPressure() { return pressure; }
    public void setPressure(Integer v) { this.pressure = v; }
    public Double getVisibility() { return visibility; }
    public void setVisibility(Double v) { this.visibility = v; }
    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String v) { this.weatherCondition = v; }
    public LocalDateTime getSearchedAt() { return searchedAt; }
}
