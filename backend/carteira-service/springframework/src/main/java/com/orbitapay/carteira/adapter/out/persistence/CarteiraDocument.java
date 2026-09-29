package com.orbitapay.carteira.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.orbitapay.carteira.adapter.out.persistence.trava.TravaDocument;

@Document("carteiras")
public record CarteiraDocument(
        @Id String id,
        @Indexed(unique = true) String clienteId,
        List<PosicaoDocument> posicoes,
        List<String> ordensLiquidadas,
        TravaDocument trava) {

    public record PosicaoDocument(String ticker, long quantidade, BigDecimal precoMedio, List<ReservaDocument> reservas) {
    }

    public record ReservaDocument(String ordemId, long quantidade) {
    }
}
