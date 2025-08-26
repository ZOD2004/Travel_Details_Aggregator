package com.getgoods.aggeregatorservice.feign;

import com.getgoods.aggeregatorservice.entity.Weather;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "WEATHER-SERVICE")
public interface WeatherFeign {

    @GetMapping("/api/weathers/find")
    public ResponseEntity<List<Weather>> getWeatherByCountry
            (@RequestParam String country, @RequestParam String city,
             @RequestParam Integer from , @RequestParam Integer to );
}
