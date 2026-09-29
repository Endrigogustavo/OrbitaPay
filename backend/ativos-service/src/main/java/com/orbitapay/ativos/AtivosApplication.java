package com.orbitapay.ativos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AtivosApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtivosApplication.class, args);
    }
}
