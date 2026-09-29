package com.orbitapay.contas.application.port.out;

import com.orbitapay.contas.domain.model.Conta;

public record ContaTravada(Conta conta, String dono) {
}
