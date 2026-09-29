package com.orbitapay.contas.application.port.in;

import java.math.BigDecimal;

import com.orbitapay.contas.application.dto.Comprovante;

public interface SacarUseCase {

    Comprovante executar(String clienteId, BigDecimal valor);
}
