package com.weatherapp.exception;

public class CityNotFoundException extends RuntimeException {
    public CityNotFoundException(String message) { super(message); }
}
