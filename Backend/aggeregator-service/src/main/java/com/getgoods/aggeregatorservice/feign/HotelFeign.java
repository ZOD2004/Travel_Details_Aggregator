package com.getgoods.aggeregatorservice.feign;


import com.getgoods.aggeregatorservice.entity.Hotel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "HOTEL-SERVICE")
public interface HotelFeign {
    @GetMapping("/api/hotels/find")
    public ResponseEntity<List<Hotel>> findHotels(
            @RequestParam String city,@RequestParam String country,
            @RequestParam(defaultValue = "1") Integer from);
}
