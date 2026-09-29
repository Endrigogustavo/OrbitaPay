package com.orbitapay.carteira.application.port.in;

import com.orbitapay.carteira.application.dto.CarteiraValorizada;

public interface ConsultarCarteiraUseCase {

    CarteiraValorizada doCliente(String clienteId);
}
