package com.getgoods.placedetails.controller;

import com.getgoods.placedetails.model.PlaceDetails;
import com.getgoods.placedetails.service.PlaceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/details")
public class PlaceController {

    private PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/find")
    public ResponseEntity<PlaceDetails> findPlaceDetails(
            @RequestParam String city,
            @RequestParam String country){
        return new ResponseEntity<>(placeService.findPlaceDetails(city,country), HttpStatus.OK);

    }
}
