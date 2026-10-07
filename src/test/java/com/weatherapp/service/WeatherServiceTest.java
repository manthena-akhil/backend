package com.weatherapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherapp.dto.ForecastDayDto;
import com.weatherapp.dto.HourlyDto;
import com.weatherapp.dto.WeatherDto;
import com.weatherapp.exception.CityNotFoundException;
import com.weatherapp.exception.ValidationException;
import com.weatherapp.exception.WeatherApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WeatherServiceTest {
    static final String CURRENT = """
        {"coord":{"lon":78.47,"lat":17.38},"weather":[{"description":"clear sky","icon":"01d"}],
         "main":{"temp":29.04,"feels_like":31.2,"temp_min":24.0,"temp_max":32.0,"pressure":1012,"humidity":65},
         "visibility":8000,"wind":{"speed":3.33},"sys":{"country":"IN","sunrise":1700000000,"sunset":1700043000},
         "timezone":19800,"name":"Hyderabad"}""";
    static final String FORECAST = """
        {"city":{"timezone":0},"list":[
         {"dt":1700000000,"main":{"temp":20,"temp_min":19,"temp_max":21,"humidity":60},"weather":[{"description":"light rain","icon":"10n"}],"wind":{"speed":2},"pop":0.4},
         {"dt":1700003600,"main":{"temp":22,"temp_min":21,"temp_max":23,"humidity":50},"weather":[{"description":"clear sky","icon":"01d"}],"wind":{"speed":5},"pop":0.0},
         {"dt":1700100000,"main":{"temp":25,"temp_min":24,"temp_max":26,"humidity":40},"weather":[{"description":"few clouds","icon":"02d"}],"wind":{"speed":1},"pop":0.1}]}""";

    final ObjectMapper om = new ObjectMapper();

    WeatherService offline() { return new WeatherService(RestClient.create("http://localhost"), "key"); }

    @Test
    void mapsCurrentWeatherToCleanDto() throws Exception {
        WeatherDto w = offline().mapCurrent(om.readTree(CURRENT));
        assertEquals("Hyderabad", w.city());
        assertEquals("India", w.country());
        assertEquals(29.0, w.temperature());
        assertEquals(12.0, w.windSpeed());      // 3.33 m/s -> 12 km/h
        assertEquals(8.0, w.visibility());      // metres -> km
        assertEquals("Clear Sky", w.condition());
        assertTrue(w.sunrise().matches("\\d{2}:\\d{2} (AM|PM)"));
    }

    @Test
    void mapsHourlyForecast() throws Exception {
        List<HourlyDto> hours = offline().mapHourly(om.readTree(FORECAST));
        assertEquals(3, hours.size());
        assertEquals(40, hours.get(0).precipitationProbability());
        assertEquals("Light Rain", hours.get(0).condition());
    }

    @Test
    void groupsForecastByDay() throws Exception {
        List<ForecastDayDto> days = offline().mapDaily(om.readTree(FORECAST));
        assertEquals(2, days.size());
        assertEquals(23.0, days.get(0).maxTemperature());
        assertEquals(19.0, days.get(0).minTemperature());
    }

    @Test
    void emptyCityIsRejected() {
        assertThrows(ValidationException.class, () -> offline().currentByCity("   "));
    }

    @Test
    void missingApiKeyGivesFriendlyError() {
        WeatherService s = new WeatherService(RestClient.create("http://localhost"), "");
        assertThrows(WeatherApiException.class, () -> s.currentByCity("Delhi"));
    }

    @Test
    void invalidCityThrowsCityNotFound() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://api.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("q=Atlantis"))).andRespond(withStatus(HttpStatus.NOT_FOUND));
        WeatherService s = new WeatherService(builder.build(), "key");
        assertThrows(CityNotFoundException.class, () -> s.currentByCity("Atlantis"));
        server.verify();
    }

    @Test
    void successfulCallReturnsWeatherAndServerErrorsBecomeServiceUnavailable() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://api.test");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(containsString("q=Hyderabad"))).andRespond(withSuccess(CURRENT, MediaType.APPLICATION_JSON));
        server.expect(requestTo(containsString("q=Boom"))).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        WeatherService s = new WeatherService(builder.build(), "key");
        assertEquals("Hyderabad", s.currentByCity("Hyderabad").city());
        assertThrows(WeatherApiException.class, () -> s.currentByCity("Boom"));
    }
}
