package com.orbitapay.negociacao.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;

@Configuration
public class TransacaoConfig {

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory mongo) {
        return new MongoTransactionManager(mongo);
    }
}
