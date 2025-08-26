package com.getgoods.weatherservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "weather")
public class Weather {

    @Id
    private WeatherId id;

    private String maxTemp;
    private String minTemp;
    private String desc;
    private String day;
    private String travelAdvice;
    private String feelsLike;
    private String others;
}
