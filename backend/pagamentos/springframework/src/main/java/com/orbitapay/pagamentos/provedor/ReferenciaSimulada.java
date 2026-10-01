package com.orbitapay.pagamentos.provedor;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Os provedores simulados não guardam estado: o identificador que cada um gera carrega o instante de emissão, o
 * valor e a validade, e a situação é recalculada a cada consulta. Assim a simulação sobrevive a reinícios e
 * funciona com várias instâncias do serviço.
 */
public record ReferenciaSimulada(Instant emitidaEm, long centavos, int validadeSegundos) {

    private static final int DIGITOS_DO_INSTANTE = 10;
    private static final int DIGITOS_DO_VALOR = 11;
    private static final int DIGITOS_DA_VALIDADE = 6;
    private static final int TAMANHO = DIGITOS_DO_INSTANTE + DIGITOS_DO_VALOR + DIGITOS_DA_VALIDADE;

    public String codificar(String prefixo, int digitosAleatorios) {
        StringBuilder aleatorio = new StringBuilder();
        for (int i = 0; i < digitosAleatorios; i++) {
            aleatorio.append(ThreadLocalRandom.current().nextInt(10));
        }
        return prefixo + String.format("%0" + DIGITOS_DO_INSTANTE + "d%0" + DIGITOS_DO_VALOR + "d%0"
                + DIGITOS_DA_VALIDADE + "d", emitidaEm.getEpochSecond(), centavos, validadeSegundos) + aleatorio;
    }

    public static ReferenciaSimulada decodificar(String prefixo, String referencia) {
        if (referencia == null || !referencia.startsWith(prefixo) || referencia.length() < prefixo.length() + TAMANHO) {
            throw new ErroDoProvedor(404, "NAO_ENCONTRADO", "Referência desconhecida: " + referencia);
        }
        try {
            int i = prefixo.length();
            long epoch = Long.parseLong(referencia.substring(i, i += DIGITOS_DO_INSTANTE));
            long centavos = Long.parseLong(referencia.substring(i, i += DIGITOS_DO_VALOR));
            int validade = Integer.parseInt(referencia.substring(i, i + DIGITOS_DA_VALIDADE));
            return new ReferenciaSimulada(Instant.ofEpochSecond(epoch), centavos, validade);
        } catch (NumberFormatException e) {
            throw new ErroDoProvedor(404, "NAO_ENCONTRADO", "Referência desconhecida: " + referencia);
        }
    }

    public Instant validaAte() {
        return emitidaEm.plusSeconds(validadeSegundos);
    }

    public Instant liquidacao(int segundosParaLiquidar) {
        return emitidaEm.plusSeconds(segundosParaLiquidar);
    }

    /** O pagador simulado desiste dos valores terminados em 99 centavos, para exercitar a expiração. */
    public boolean liquidadaEm(Instant agora, int segundosParaLiquidar) {
        Instant liquidacao = liquidacao(segundosParaLiquidar);
        return centavos % 100 != 99 && !agora.isBefore(liquidacao) && !liquidacao.isAfter(validaAte());
    }

    public boolean vencidaEm(Instant agora) {
        return agora.isAfter(validaAte());
    }
}
