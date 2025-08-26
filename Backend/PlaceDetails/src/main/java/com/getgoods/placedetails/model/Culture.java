package com.getgoods.placedetails.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Culture {
    private String id;
    private String name;
    private String description;
    private String city;
    private String category;
    private String historicalPeriod;
}
