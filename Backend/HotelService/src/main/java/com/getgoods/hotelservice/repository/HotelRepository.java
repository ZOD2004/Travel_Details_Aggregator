package com.getgoods.hotelservice.repository;

import com.getgoods.hotelservice.model.Hotels;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends MongoRepository<Hotels,String> {
    Optional<Hotels> findByAddress(String address);
    Optional<List<Hotels>> findByCityAndCountryAndDate(String city, String country,Integer date);
}
