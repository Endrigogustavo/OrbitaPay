package com.orbitapay.negociacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NegociacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NegociacaoApplication.class, args);
    }
}
