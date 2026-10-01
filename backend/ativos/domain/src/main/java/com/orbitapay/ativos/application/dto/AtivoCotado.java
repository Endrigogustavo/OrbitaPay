package com.orbitapay.ativos.application.dto;

import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Bolsa;

public record AtivoCotado(Ativo ativo, Bolsa bolsa) {
}
