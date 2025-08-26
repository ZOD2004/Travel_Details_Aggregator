package com.getgoods.aggeregatorservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Weather {
    private String maxTemp;
    private String minTemp;
    private String desc;
    private String day;
    private String travelAdvice;
    private String feelsLike;
    private String others;
}
