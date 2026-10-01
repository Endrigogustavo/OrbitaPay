package com.orbitapay.clientes.web.dto;

import java.math.BigDecimal;

public record CadastroRequest(String nome, String email, String cpf, String pin, BigDecimal depositoInicial) {
}
