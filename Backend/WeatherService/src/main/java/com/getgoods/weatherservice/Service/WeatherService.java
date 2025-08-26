package com.getgoods.weatherservice.Service;

import com.getgoods.weatherservice.Model.Weather;
import com.getgoods.weatherservice.Model.WeatherId;
import com.getgoods.weatherservice.Repository.WeatherRepository;
import com.getgoods.weatherservice.scraper.WeatherScraper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherService {

    private WeatherRepository weatherRepository;
    private WeatherScraper weatherScraper;
    public WeatherService(WeatherRepository weatherRepository, WeatherScraper weatherScraper) {
        this.weatherRepository = weatherRepository;
        this.weatherScraper = weatherScraper;
    }


    public List<Weather> getInfo(String country, String city, Integer from, Integer to) {
        List<Weather> weatherList = new ArrayList<>();
        List<WeatherId> ids = new ArrayList<>();
        for(int i=from; i<=to; i++){
            WeatherId weatherId = new WeatherId(String.valueOf(i),country,city);
            ids.add(weatherId);
        }
        for(WeatherId weatherId : ids){
            Optional<Weather> weather = weatherRepository.findById(weatherId);
            if(weather.isPresent()){
                weatherList.add(weather.get());
            }
            else{
                Weather w = weatherScraper.findByWeatherId(weatherId);
                weatherList.add(w);
                weatherRepository.save(w);
            }
        }
        return weatherList;

    }
}
