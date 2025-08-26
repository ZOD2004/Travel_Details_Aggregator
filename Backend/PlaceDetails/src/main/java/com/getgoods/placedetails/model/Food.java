package com.getgoods.placedetails.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Food {
    private String id;
    private String name;
    private String description;
    private String city;
    private String cuisineType;
    private String priceRange;
}
