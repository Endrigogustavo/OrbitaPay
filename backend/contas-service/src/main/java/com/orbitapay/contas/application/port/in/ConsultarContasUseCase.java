package com.orbitapay.contas.application.port.in;

import java.util.List;

import com.orbitapay.contas.domain.model.Conta;

public interface ConsultarContasUseCase {

    Conta porCliente(String clienteId);

    List<Conta> listar();
}
