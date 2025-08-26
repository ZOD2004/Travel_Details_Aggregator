package com.getgoods.aggeregatorservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Food {
    private String name;
    private String description;
    private String city;
    private String cuisineType;
    private String priceRange;
}
