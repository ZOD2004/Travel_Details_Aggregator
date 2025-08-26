package com.getgoods.aggeregatorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.getgoods.aggeregatorservice.feign")
public class AggeregatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggeregatorServiceApplication.class, args);
    }

}
