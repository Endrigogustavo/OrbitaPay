package com.orbitapay.carteira.persistence;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("ativos_cotados")
public record AtivoCotadoDocument(@Id String ticker, String nome, String moeda, BigDecimal cambio, BigDecimal cotacao) {
}
