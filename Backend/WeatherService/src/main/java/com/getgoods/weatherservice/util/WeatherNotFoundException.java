package com.getgoods.weatherservice.util;

public class WeatherNotFoundException extends RuntimeException {
    public WeatherNotFoundException(String message) {
        super(message);
    }
}
