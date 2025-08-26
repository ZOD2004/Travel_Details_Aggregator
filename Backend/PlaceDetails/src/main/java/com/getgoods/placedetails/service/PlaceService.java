package com.getgoods.placedetails.service;

import com.getgoods.placedetails.model.PlaceDetails;
import com.getgoods.placedetails.repository.PlaceRepository;
import com.getgoods.placedetails.scraper.PlaceDetailsScraper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlaceService {

    private PlaceRepository placeRepository;
    private PlaceDetailsScraper placeDetailsScraper;


    public PlaceService(PlaceRepository placeRepository, PlaceDetailsScraper placeDetailsScraper) {
        this.placeRepository = placeRepository;
        this.placeDetailsScraper = placeDetailsScraper;
    }

    public PlaceDetails findPlaceDetails(String city, String country) {

        Optional<PlaceDetails> placeDetails = placeRepository.findByCountryAndCity(country,city);

        if(placeDetails.isPresent()){
            return  placeDetails.get();
        }
        else{
            PlaceDetails p = placeDetailsScraper.findPlaceDetails(city, country);
            placeRepository.save(p);
            return p;
        }

    }
}
