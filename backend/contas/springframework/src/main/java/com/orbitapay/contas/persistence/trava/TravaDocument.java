package com.orbitapay.contas.persistence.trava;

import java.time.Instant;

public record TravaDocument(String dono, Instant adquiridaEm, Instant expiraEm) {
}
