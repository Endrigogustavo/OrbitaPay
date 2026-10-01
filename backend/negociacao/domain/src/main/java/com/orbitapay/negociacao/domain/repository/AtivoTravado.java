package com.orbitapay.negociacao.domain.repository;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;

public record AtivoTravado(AtivoNegociavel ativo, String dono) {
}
