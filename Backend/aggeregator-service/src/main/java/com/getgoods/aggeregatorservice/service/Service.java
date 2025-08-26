package com.getgoods.aggeregatorservice.service;

import com.getgoods.aggeregatorservice.entity.Hotel;
import com.getgoods.aggeregatorservice.entity.PlaceDetails;
import com.getgoods.aggeregatorservice.entity.Weather;
import com.getgoods.aggeregatorservice.feign.HotelFeign;
import com.getgoods.aggeregatorservice.feign.PlaceFeign;
import com.getgoods.aggeregatorservice.feign.WeatherFeign;
import com.getgoods.aggeregatorservice.model.Aggregator;
import com.getgoods.aggeregatorservice.repository.Repository;
import com.getgoods.aggeregatorservice.util.HotelsNotFoundException;
import com.getgoods.aggeregatorservice.util.PlaceDetailsNotFoundException;
import com.getgoods.aggeregatorservice.util.WeatherNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@org.springframework.stereotype.Service
public class Service {

    private Repository repository;
    private HotelFeign  hotelFeign;
    private PlaceFeign  placeFeign;
    private WeatherFeign weatherFeign;

    public Service(Repository repository,HotelFeign hotelFeign,PlaceFeign placeFeign,WeatherFeign weatherFeign) {
        this.repository = repository;
        this.hotelFeign = hotelFeign;
        this.placeFeign = placeFeign;
        this.weatherFeign = weatherFeign;
    }


    public Aggregator findDetails(String country, String city, Integer from, Integer to) {
        Aggregator aggregator = new Aggregator();

        Optional<Aggregator>op = repository.findByCityAndCountry(city, country);
        if(op.isPresent()) {
            aggregator = op.get();
        }
        else{
            List<Weather> weathers = null;
            try {
                weathers = weatherFeign.getWeatherByCountry(country, city, from, to).getBody();
            } catch (Exception e) {
                throw new WeatherNotFoundException("weather not found for country " + country + " and city " + city);
            }
            PlaceDetails placeDetails = null;
            try {
                placeDetails = placeFeign.findPlaceDetails(city, country).getBody();
            } catch (Exception e) {
                throw new PlaceDetailsNotFoundException("place details not found for country " + country + " and city " + city);
            }
            List<Hotel> hotels = null;
            try {
                hotels = hotelFeign.findHotels(city, country, from).getBody();
            } catch (Exception e) {
//                throw new HotelsNotFoundException("hotels not found for country " + country + " and city " + city);
            }
            aggregator.setId(UUID.randomUUID().toString());
            aggregator.setCity(city);
            aggregator.setCountry(country);
            aggregator.setHotel(hotels);
            aggregator.setPlaceDetails(placeDetails);
            aggregator.setWeather(weathers);
        }

        return aggregator;
    }

    public List<Aggregator> fetchAll() {
        return  repository.findAll();
    }
}
