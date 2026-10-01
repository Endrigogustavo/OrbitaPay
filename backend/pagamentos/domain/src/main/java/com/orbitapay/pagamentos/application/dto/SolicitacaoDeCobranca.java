package com.orbitapay.pagamentos.application.dto;

import com.orbitapay.pagamentos.domain.model.Dinheiro;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;

public record SolicitacaoDeCobranca(String pagamentoId, String clienteId, Dinheiro valor, MetodoDePagamento metodo) {
}
