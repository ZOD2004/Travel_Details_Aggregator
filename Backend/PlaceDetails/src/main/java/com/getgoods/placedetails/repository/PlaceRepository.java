package com.getgoods.placedetails.repository;

import com.getgoods.placedetails.model.PlaceDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends MongoRepository<PlaceDetails, String> {
    public Optional<PlaceDetails> findByCountryAndCity(String country, String city);
}
