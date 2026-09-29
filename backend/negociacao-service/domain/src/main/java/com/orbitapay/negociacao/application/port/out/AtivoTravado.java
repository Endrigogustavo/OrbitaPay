package com.orbitapay.negociacao.application.port.out;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;

public record AtivoTravado(AtivoNegociavel ativo, String dono) {
}
