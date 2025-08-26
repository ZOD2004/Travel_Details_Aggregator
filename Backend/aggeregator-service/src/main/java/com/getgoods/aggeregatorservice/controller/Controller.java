package com.getgoods.aggeregatorservice.controller;

import com.getgoods.aggeregatorservice.model.Aggregator;
import com.getgoods.aggeregatorservice.service.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agg")
@CrossOrigin(origins = "*")
public class Controller {

    private Service service;

    public Controller(Service service) {
        this.service = service;
    }

    @GetMapping("/find")
    public ResponseEntity<Aggregator> get(
            @RequestParam String country,
            @RequestParam String city,
            @RequestParam(defaultValue = "1") Integer from,
            @RequestParam(defaultValue = "30") Integer to
    ) {
        System.out.println("Incontroller");
        return new ResponseEntity<>(service.findDetails(country,city,from,to), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Aggregator>> fetchAll() {
        return new ResponseEntity<>(service.fetchAll(),HttpStatus.OK);
    }
}
