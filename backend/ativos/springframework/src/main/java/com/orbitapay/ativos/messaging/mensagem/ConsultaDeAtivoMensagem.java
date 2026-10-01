package com.orbitapay.ativos.messaging.mensagem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsultaDeAtivoMensagem(String ticker, String solicitante) {
}
