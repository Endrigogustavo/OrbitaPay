package com.orbitapay.contas.application.port.in;

import java.math.BigDecimal;

import com.orbitapay.contas.application.dto.Comprovante;

public interface DepositarUseCase {

    Comprovante executar(String clienteId, BigDecimal valor, String metodo);
}
