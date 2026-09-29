package com.orbitapay.clientes.application.port.in;

import java.math.BigDecimal;

import com.orbitapay.clientes.domain.model.Cliente;

public interface CadastrarClienteUseCase {

    Cliente executar(Comando comando);

    record Comando(String nome, String email, String cpf, String pin, BigDecimal depositoInicial) {
    }
}
