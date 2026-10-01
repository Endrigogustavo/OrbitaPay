package com.orbitapay.pagamentos.provedor.ted.modelo;

/**
 * Modelo do SPB simulado para depósitos por TED: o banco gera um identificador de depósito; o valor trafega como
 * texto no formato brasileiro ("1.234,56"), datas como {@code dd/MM/yyyy HH:mm:ss} e a situação como código
 * numérico (0 aguardando crédito, 1 creditado, 9 cancelado).
 */
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
