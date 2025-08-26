package com.getgoods.aggeregatorservice.feign;

import com.getgoods.aggeregatorservice.entity.PlaceDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name  = "PLACE-SERVICE")
public interface PlaceFeign {
    @GetMapping("/api/details/find")
    public ResponseEntity<PlaceDetails> findPlaceDetails(
            @RequestParam String city,
            @RequestParam String country);

}
