package com.getgoods.hotelservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "hotel")
public class Hotels {
    @Id
    private String id;
    private String address;
    private String city;
    private String hotelName;
    private String hotelDescription;
    private String country;
    private Long price;
    private String type;
    private String url;
    private String date;
}
