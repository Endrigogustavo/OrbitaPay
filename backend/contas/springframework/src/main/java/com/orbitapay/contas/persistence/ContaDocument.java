package com.orbitapay.contas.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.orbitapay.contas.persistence.trava.TravaDocument;

@Document("contas")
public record ContaDocument(
        @Id String id,
        @Indexed(unique = true) String clienteId,
        String nomeTitular,
        boolean titularBloqueado,
        @Indexed(unique = true) String numero,
        BigDecimal saldo,
        List<LancamentoDocument> lancamentos,
        Instant abertaEm,
        TravaDocument trava) {
}
