package com.orbitapay.ativos.adapter.in.messaging.mensagem;

import java.math.BigDecimal;

import com.orbitapay.ativos.application.dto.AtivoCotado;

public record AtivoConsultadoMensagem(
        boolean encontrado,
        String ticker,
        String nome,
        String setor,
        String bolsa,
        String moeda,
        BigDecimal cambio,
        BigDecimal cotacao,
        long quantidadeEmitida) {

    public static AtivoConsultadoMensagem de(AtivoCotado cotado) {
        return new AtivoConsultadoMensagem(true, cotado.ativo().ticker().valor(), cotado.ativo().nome(),
                cotado.ativo().setor(), cotado.bolsa().codigo(), cotado.bolsa().moeda(), cotado.bolsa().cambio(),
                cotado.ativo().cotacao(), cotado.ativo().quantidadeEmitida());
    }

    public static AtivoConsultadoMensagem naoEncontrado(String ticker) {
        return new AtivoConsultadoMensagem(false, ticker, null, null, null, null, null, null, 0);
    }
}
