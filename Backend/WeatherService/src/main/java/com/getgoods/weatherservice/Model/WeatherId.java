package com.getgoods.weatherservice.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherId implements Serializable {
    private String date;
    private String country;
    private String city;
}
