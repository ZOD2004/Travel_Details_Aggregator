package com.getgoods.aggeregatorservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Hotel {
    private String address;
    private String hotelName;
    private String hotelDescription;;
    private Long price;
    private String type;
    private String url;
    private String date;
}
