package com.example.kiosk_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class KioskBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KioskBackendApplication.class, args);
    }

}
