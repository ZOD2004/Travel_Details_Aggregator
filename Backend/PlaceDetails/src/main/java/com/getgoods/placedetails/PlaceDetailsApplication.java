package com.getgoods.placedetails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PlaceDetailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaceDetailsApplication.class, args);
    }

}
