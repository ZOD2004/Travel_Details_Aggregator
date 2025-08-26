package com.getgoods.weatherservice.Controller;


import com.getgoods.weatherservice.Model.Weather;
import com.getgoods.weatherservice.Service.WeatherService;
import jakarta.ws.rs.DefaultValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weathers")
public class WeatherController {

    private WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }



    @GetMapping("/find")
    public ResponseEntity<List<Weather>> getWeatherByCountry(@RequestParam String country
            , @RequestParam String city, @RequestParam(defaultValue = "1") Integer from
            , @RequestParam(defaultValue = "28") Integer to ) {
//        System.out.println("weather controller");

        return new ResponseEntity<>(weatherService.getInfo(country,city,from,to), HttpStatus.OK);
    }
}

