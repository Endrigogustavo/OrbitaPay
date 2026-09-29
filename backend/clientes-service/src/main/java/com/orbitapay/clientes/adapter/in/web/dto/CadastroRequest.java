package com.orbitapay.clientes.adapter.in.web.dto;

import java.math.BigDecimal;

public record CadastroRequest(String nome, String email, String cpf, String pin, BigDecimal depositoInicial) {
}
