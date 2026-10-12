package com.orbitapay.clientes.web.dto;

import java.math.BigDecimal;

public record CadastroRequest(String clienteId, String nome, String email, String cpf, BigDecimal depositoInicial) {
}
