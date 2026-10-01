package com.orbitapay.pagamentos.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * O que o cliente precisa para pagar, já na linguagem do OrbitaPay: o código a copiar (Pix copia e cola,
 * linha digitável do boleto ou os dados da conta para a TED), uma explicação curta e o prazo.
 */
public record InstrucoesDePagamento(String codigo, String descricao, Instant validoAte) {

    public InstrucoesDePagamento {
        Objects.requireNonNull(codigo);
        Objects.requireNonNull(descricao);
        Objects.requireNonNull(validoAte);
    }
}
