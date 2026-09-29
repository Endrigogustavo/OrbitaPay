package com.orbitapay.carteira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CarteiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarteiraApplication.class, args);
    }
}
