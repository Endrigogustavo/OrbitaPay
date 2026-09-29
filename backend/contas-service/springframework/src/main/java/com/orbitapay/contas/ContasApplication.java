package com.orbitapay.contas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ContasApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContasApplication.class, args);
    }
}
