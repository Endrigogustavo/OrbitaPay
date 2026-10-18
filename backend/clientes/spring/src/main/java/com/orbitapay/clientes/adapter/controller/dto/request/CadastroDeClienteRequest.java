package com.orbitapay.clientes.adapter.controller.dto.request;

import java.math.BigDecimal;

public record CadastroDeClienteRequest(String clienteId, String nome, String email, String cpf,
        BigDecimal depositoInicial) {
}
