package com.orbitapay.ativos.adapter.in.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsultaDeAtivoMensagem(String ticker, String solicitante) {
}
