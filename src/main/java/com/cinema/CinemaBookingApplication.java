package com.cinema;

import com.cinema.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(StorageProperties.class)
@EnableScheduling
public class CinemaBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CinemaBookingApplication.class, args);
    }
}
