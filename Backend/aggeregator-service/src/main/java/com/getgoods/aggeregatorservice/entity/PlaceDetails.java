package com.getgoods.aggeregatorservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDetails {
    private List<Culture> cultures;
    private List<Food> foods;
    private List<TouristAttraction>touristAttractions;
}
