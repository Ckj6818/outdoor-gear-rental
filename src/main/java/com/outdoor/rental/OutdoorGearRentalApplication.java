package com.outdoor.rental;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.outdoor.rental.mapper")
public class OutdoorGearRentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(OutdoorGearRentalApplication.class, args);
    }
}
