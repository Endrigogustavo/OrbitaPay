package com.orbitapay.pagamentos.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public Dinheiro {
        valor = (valor == null ? BigDecimal.ZERO : valor).setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro deCentavos(long centavos) {
        return new Dinheiro(BigDecimal.valueOf(centavos, 2));
    }

    public long emCentavos() {
        return valor.movePointRight(2).longValueExact();
    }

    public boolean positivo() {
        return valor.signum() > 0;
    }

    public boolean maiorQue(Dinheiro outro) {
        return compareTo(outro) > 0;
    }

    public boolean menorQue(Dinheiro outro) {
        return compareTo(outro) < 0;
    }

    public String formatado() {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR")));
        return "R$ " + formato.format(valor);
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }
}
