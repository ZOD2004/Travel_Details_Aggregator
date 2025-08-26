package com.getgoods.aggeregatorservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Culture {
    private String name;
    private String description;
    private String city;
    private String category;
    private String historicalPeriod;
}
