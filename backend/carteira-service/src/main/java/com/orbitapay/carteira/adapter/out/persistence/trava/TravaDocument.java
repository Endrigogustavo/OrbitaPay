package com.orbitapay.carteira.adapter.out.persistence.trava;

import java.time.Instant;

public record TravaDocument(String dono, Instant adquiridaEm, Instant expiraEm) {
}
