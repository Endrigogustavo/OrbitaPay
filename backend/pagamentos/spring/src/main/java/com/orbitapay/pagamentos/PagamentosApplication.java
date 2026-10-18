package com.orbitapay.pagamentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PagamentosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagamentosApplication.class, args);
    }
}
