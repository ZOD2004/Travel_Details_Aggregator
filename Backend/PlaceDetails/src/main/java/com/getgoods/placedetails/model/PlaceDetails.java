package com.getgoods.placedetails.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "place_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDetails {
    private List<Culture> cultures;
    private List<Food> foods;
    private List<TouristAttraction>touristAttractions;
    private String city;
    private String country;
}
