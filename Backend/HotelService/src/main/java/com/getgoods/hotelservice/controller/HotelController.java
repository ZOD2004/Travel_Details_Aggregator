package com.getgoods.hotelservice.controller;

import com.getgoods.hotelservice.model.Hotels;
import com.getgoods.hotelservice.repository.HotelRepository;
import com.getgoods.hotelservice.service.HotelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private HotelService  hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }



    //https://www.hostelworld.com/pwa/s?q=Bangalore,%20India&country=Bangalore&
    // city=Bangalore&type=city&id=2105&from=2025-08-23&to=2025-08-26&guests=2&page=1&sort=rating

    @GetMapping("/find")
    public ResponseEntity<List<Hotels>> findHotels(
            @RequestParam String city,@RequestParam String country,
            @RequestParam(defaultValue = "1") Integer from) {
        return new ResponseEntity<>(hotelService.findHotels(city,country,from), HttpStatus.OK);
    }
}
