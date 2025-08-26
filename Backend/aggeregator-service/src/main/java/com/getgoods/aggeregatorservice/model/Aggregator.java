package com.getgoods.aggeregatorservice.model;

import com.getgoods.aggeregatorservice.entity.Hotel;
import com.getgoods.aggeregatorservice.entity.PlaceDetails;
import com.getgoods.aggeregatorservice.entity.Weather;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "aggregator")
public class Aggregator {
    @Id

    private String id;
    private String city;
    private String country;
    PlaceDetails placeDetails;
    List<Weather> weather;
    List<Hotel> hotel;
}
