package com.orbitapay.clientes.infrastructure.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class MongoTransactionConfig {

    @Bean
    public MongoTransactionManager gerenciadorDeTransacoesDoMongo(MongoDatabaseFactory fabricaDoBanco) {
        return new MongoTransactionManager(fabricaDoBanco);
    }

    @Bean
    public TransactionTemplate modeloDeTransacao(MongoTransactionManager gerenciadorDeTransacoesDoMongo) {
        return new TransactionTemplate(gerenciadorDeTransacoesDoMongo);
    }
}
