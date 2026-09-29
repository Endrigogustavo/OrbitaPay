package com.orbitapay.contas.application.dto;

import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Lancamento;

public record Comprovante(Conta conta, Lancamento lancamento) {
}
