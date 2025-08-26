package com.getgoods.aggeregatorservice.repository;

import com.getgoods.aggeregatorservice.model.Aggregator;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

@org.springframework.stereotype.Repository
public interface Repository extends MongoRepository<Aggregator,String> {

    Optional<Aggregator> findByCityAndCountry(String city, String country);
}
