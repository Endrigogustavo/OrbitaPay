package com.orbitapay.negociacao.entity;

import java.util.List;

public record Carteira(String clienteId, List<Posicao> posicoes) {
}
