package com.orbitapay.pagamentos.provedor.boleto.modelo;

/**
 * Modelo do banco emissor de boletos (no estilo das APIs de cobrança bancária): valores em centavos, datas
 * {@code yyyy-MM-dd}, data e hora de liquidação no horário de Brasília sem fuso e situação com códigos próprios.
 */
public final class Boleto {

    private Boleto() {
    }

    public record RegistroRequest(String seuNumero, long valorNominalEmCentavos, String dataDeVencimento,
            String identificadorDoPagador) {
    }

    public record RegistroResponse(String nossoNumero, String codigoDeBarras, String linhaDigitavel,
            String dataDeVencimento) {
    }

    public record SituacaoResponse(String nossoNumero, String situacao, Long valorPagoEmCentavos,
            String dataHoraDaLiquidacao) {
    }

    public static final String EM_ABERTO = "EM_ABERTO";
    public static final String LIQUIDADO = "LIQUIDADO";
    public static final String BAIXADO_POR_DECURSO_DE_PRAZO = "BAIXADO_POR_DECURSO_DE_PRAZO";
}
