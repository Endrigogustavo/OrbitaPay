package com.orbitapay.contas.messaging.mensagem;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PagamentoConfirmadoMensagem(
        String eventoId,
        String evento,
        String pagamentoId,
        String clienteId,
        String metodo,
        BigDecimal valor,
        BigDecimal valorPago) {

    public BigDecimal valorRecebido() {
        return valorPago == null ? valor : valorPago;
    }

    public String rotuloDoMetodo() {
        return switch (metodo == null ? "" : metodo) {
            case "PIX" -> "Pix";
            case "BOLETO" -> "Boleto";
            default -> metodo;
        };
    }
}
