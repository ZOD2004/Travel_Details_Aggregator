package com.getgoods.weatherservice.Repository;

import com.getgoods.weatherservice.Model.Weather;
import com.getgoods.weatherservice.Model.WeatherId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends MongoRepository<Weather, WeatherId> {
}
