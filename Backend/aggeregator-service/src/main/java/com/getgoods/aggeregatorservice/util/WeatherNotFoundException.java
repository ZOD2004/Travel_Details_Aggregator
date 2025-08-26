package com.getgoods.aggeregatorservice.util;

public class WeatherNotFoundException extends RuntimeException {
    public WeatherNotFoundException(String message) {
        super(message);
    }
}
