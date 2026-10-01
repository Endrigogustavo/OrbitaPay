package com.orbitapay.contas.domain.repository;

import com.orbitapay.contas.domain.model.Conta;

public record ContaTravada(Conta conta, String dono) {
}
