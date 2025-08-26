package com.getgoods.hotelservice.service;


import com.getgoods.hotelservice.model.Hotels;
import com.getgoods.hotelservice.repository.HotelRepository;
import com.getgoods.hotelservice.scraper.HotelScrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    private HotelRepository hotelRepository;
    private HotelScrapper hotelScrapper;

    public HotelService(HotelRepository hotelRepository, HotelScrapper hotelScrapper) {
        this.hotelRepository = hotelRepository;
        this.hotelScrapper = hotelScrapper;
    }

    public List<Hotels> findHotels(String city, String country, Integer from) {
        List<Hotels> listHotels = new ArrayList<>();
            Optional<List<Hotels>> hotels = hotelRepository.findByCityAndCountryAndDate(city, country,from);
            if(hotels.isPresent() && !hotels.get().isEmpty()) {
                listHotels =  hotels.get();
            }
            else{
                List<Hotels>curr  = hotelScrapper.findHotels(city,country,from);
                listHotels = curr;
                hotelRepository.saveAll(curr);
            }

        return  listHotels;
    }
}