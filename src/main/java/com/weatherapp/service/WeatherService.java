package com.weatherapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.weatherapp.dto.ForecastDayDto;
import com.weatherapp.dto.HourlyDto;
import com.weatherapp.dto.WeatherDto;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.ValidationException;
import com.weatherapp.exception.WeatherApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Talks to OpenWeatherMap (free tier) and converts its responses into small DTOs. */
@Service
public class WeatherService {
    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    private record Cached(JsonNode body, long at) {}

    private final RestClient client;
    private final String apiKey;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    @Autowired
    public WeatherService(@Value("${app.weather.api-key}") String apiKey,
                          @Value("${app.weather.base-url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(8000);
        this.client = RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
        this.apiKey = apiKey;
    }

    /** Used by tests to supply a mock-backed client. */
    WeatherService(RestClient client, String apiKey) {
        this.client = client;
        this.apiKey = apiKey;
    }

    // ------------------------------------------------------------------ public API

    public WeatherDto currentByCity(String city) {
        return mapCurrent(fetch("/weather", Map.of("q", cleanCity(city))));
    }

    public WeatherDto currentByCoordinates(double lat, double lon) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) throw new ValidationException("Invalid coordinates");
        return mapCurrent(fetch("/weather", Map.of("lat", String.valueOf(lat), "lon", String.valueOf(lon))));
    }

    public List<ForecastDayDto> dailyForecast(String city) {
        return mapDaily(fetch("/forecast", Map.of("q", cleanCity(city))));
    }

    public List<HourlyDto> hourlyForecast(String city) {
        return mapHourly(fetch("/forecast", Map.of("q", cleanCity(city))));
    }

    // ------------------------------------------------------------------ HTTP

    private String cleanCity(String city) {
        String c = city == null ? "" : city.trim();
        if (c.isEmpty()) throw new ValidationException("Please enter a city name.");
        if (c.length() > 100) throw new ValidationException("City name is too long.");
        return c;
    }

    private JsonNode fetch(String path, Map<String, String> params) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("WEATHER_API_KEY is not configured");
            throw new WeatherApiException("Weather service is temporarily unavailable.");
        }
        String cacheKey = path + new TreeMap<>(params);
        Cached hit = cache.get(cacheKey);
        if (hit != null && System.currentTimeMillis() - hit.at() < CACHE_TTL_MS) return hit.body();

        StringBuilder template = new StringBuilder(path).append('?');
        Map<String, Object> vars = new HashMap<>(params);
        params.keySet().forEach(k -> template.append(k).append("={").append(k).append("}&"));
        template.append("appid={appid}&units=metric");
        vars.put("appid", apiKey);

        try {
            JsonNode body = client.get().uri(template.toString(), vars).retrieve().body(JsonNode.class);
            if (body == null) throw new WeatherApiException("Unable to retrieve weather information.");
            if (cache.size() > 200) cache.clear();
            cache.put(cacheKey, new Cached(body, System.currentTimeMillis()));
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw new CityNotFoundException("City not found");
        } catch (HttpClientErrorException.BadRequest e) {
            throw new CityNotFoundException("City not found");
        } catch (HttpClientErrorException e) {
            log.error("Weather API rejected the request: {}", e.getStatusCode());
            throw new WeatherApiException("Weather service is temporarily unavailable.");
        } catch (RestClientException e) {
            log.error("Weather API call failed: {}", e.getMessage());
            throw new WeatherApiException("Weather service is temporarily unavailable.");
        }
    }

    // ------------------------------------------------------------------ mapping

    WeatherDto mapCurrent(JsonNode n) {
        int tz = n.path("timezone").asInt(0);
        JsonNode main = n.path("main");
        JsonNode w = n.path("weather").path(0);
        JsonNode sys = n.path("sys");
        double visibilityKm = round1(n.path("visibility").asDouble(10000) / 1000.0);
        return new WeatherDto(
                n.path("name").asText(""),
                countryName(sys.path("country").asText("")),
                n.path("coord").path("lat").asDouble(), n.path("coord").path("lon").asDouble(),
                round1(main.path("temp").asDouble()), round1(main.path("feels_like").asDouble()),
                round1(main.path("temp_min").asDouble()), round1(main.path("temp_max").asDouble()),
                main.path("humidity").asInt(), kmh(n.path("wind").path("speed").asDouble()),
                main.path("pressure").asInt(), visibilityKm,
                title(w.path("description").asText("")), w.path("icon").asText("01d"),
                format(sys.path("sunrise").asLong(), tz, "hh:mm a"),
                format(sys.path("sunset").asLong(), tz, "hh:mm a"),
                format(Instant.now().getEpochSecond(), tz, "EEE, dd MMM yyyy, hh:mm a"),
                null /* UV index is not part of the free OpenWeatherMap plan */);
    }

    List<HourlyDto> mapHourly(JsonNode n) {
        int tz = n.path("city").path("timezone").asInt(0);
        List<HourlyDto> out = new ArrayList<>();
        for (JsonNode e : n.path("list")) {
            if (out.size() >= 8) break;
            JsonNode w = e.path("weather").path(0);
            out.add(new HourlyDto(format(e.path("dt").asLong(), tz, "h a"), w.path("icon").asText("01d"),
                    title(w.path("description").asText("")), round1(e.path("main").path("temp").asDouble()),
                    (int) Math.round(e.path("pop").asDouble() * 100), kmh(e.path("wind").path("speed").asDouble()),
                    e.path("main").path("humidity").asInt()));
        }
        return out;
    }

    List<ForecastDayDto> mapDaily(JsonNode n) {
        int tz = n.path("city").path("timezone").asInt(0);
        Map<String, List<JsonNode>> byDay = new LinkedHashMap<>();
        for (JsonNode e : n.path("list")) {
            byDay.computeIfAbsent(format(e.path("dt").asLong(), tz, "yyyy-MM-dd"), k -> new ArrayList<>()).add(e);
        }
        List<ForecastDayDto> out = new ArrayList<>();
        for (List<JsonNode> entries : byDay.values()) {
            if (out.size() >= 7) break;
            double max = -Double.MAX_VALUE, min = Double.MAX_VALUE, wind = 0, hum = 0;
            JsonNode rep = entries.get(0);
            int bestDiff = Integer.MAX_VALUE;
            for (JsonNode e : entries) {
                max = Math.max(max, e.path("main").path("temp_max").asDouble());
                min = Math.min(min, e.path("main").path("temp_min").asDouble());
                wind = Math.max(wind, e.path("wind").path("speed").asDouble());
                hum += e.path("main").path("humidity").asDouble();
                int hour = Integer.parseInt(format(e.path("dt").asLong(), tz, "HH"));
                int diff = Math.abs(hour - 12);
                if (diff < bestDiff) { bestDiff = diff; rep = e; }
            }
            long dt = rep.path("dt").asLong();
            JsonNode w = rep.path("weather").path(0);
            String icon = w.path("icon").asText("01d");
            out.add(new ForecastDayDto(format(dt, tz, "EEE"), format(dt, tz, "dd MMM"),
                    icon.substring(0, 2) + "d", title(w.path("description").asText("")),
                    round1(max), round1(min), (int) Math.round(hum / entries.size()), kmh(wind)));
        }
        return out;
    }

    // ------------------------------------------------------------------ helpers

    private static String format(long epochSeconds, int tzOffsetSeconds, String pattern) {
        return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                .format(Instant.ofEpochSecond(epochSeconds).atOffset(ZoneOffset.ofTotalSeconds(tzOffsetSeconds)));
    }

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    private static double kmh(double metersPerSecond) { return round1(metersPerSecond * 3.6); }

    private static String countryName(String code) {
        if (code == null || code.isBlank()) return "";
        String name = new Locale("", code).getDisplayCountry(Locale.ENGLISH);
        return name.isBlank() ? code : name;
    }

    private static String title(String s) {
        if (s == null || s.isBlank()) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : s.trim().split("\\s+")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
