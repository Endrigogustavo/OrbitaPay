package com.orbitapay.carteira.application.port.out;

import com.orbitapay.carteira.domain.model.Carteira;

public record CarteiraTravada(Carteira carteira, String dono) {
}
