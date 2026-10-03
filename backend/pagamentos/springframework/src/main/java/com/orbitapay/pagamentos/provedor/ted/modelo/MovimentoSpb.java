package com.orbitapay.pagamentos.provedor.ted.modelo;

public final class MovimentoSpb {

    private MovimentoSpb() {
    }

    public record PedidoDeIdentificador(String referenciaDoCliente, String valorEsperado, int horasDeValidade) {
    }

    public record Identificador(
            String codigoIdentificador,
            String bancoFavorecido,
            String agenciaFavorecida,
            String contaFavorecida,
            String nomeFavorecido,
            String valorEsperado,
            String validoAte) {
    }

    public record Movimento(String codigoIdentificador, int codigoSituacao, String valorCreditado,
            String dataHoraDoCredito) {
    }

    public static final int AGUARDANDO_CREDITO = 0;
    public static final int CREDITADO = 1;
    public static final int CANCELADO = 9;
}
