package com.college.bustracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CollegeBusTrackingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollegeBusTrackingApplication.class, args);
    }
}
