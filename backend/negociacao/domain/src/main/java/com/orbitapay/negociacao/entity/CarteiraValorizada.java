package com.orbitapay.negociacao.entity;

import java.math.BigDecimal;
import java.util.List;

public record CarteiraValorizada(String clienteId, List<PosicaoValorizada> posicoes, BigDecimal valorDeMercado,
        BigDecimal custo) {
}
