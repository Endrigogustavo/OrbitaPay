package com.orbitapay.pagamentos.provedor.pix.modelo;

import java.util.List;

public final class CobrancaImediata {

    private CobrancaImediata() {
    }

    public record Requisicao(Calendario calendario, Valor valor, String chave, String solicitacaoPagador) {
    }

    public record Resposta(
            String txid,
            int revisao,
            String status,
            Calendario calendario,
            Valor valor,
            String chave,
            String pixCopiaECola,
            List<PixRecebido> pix) {
    }

    public record Calendario(String criacao, Integer expiracao) {
    }

    public record Valor(String original) {
    }

    public record PixRecebido(String endToEndId, String txid, String valor, String horario) {
    }

    public static final String ATIVA = "ATIVA";
    public static final String CONCLUIDA = "CONCLUIDA";
    public static final String REMOVIDA_PELO_PSP = "REMOVIDA_PELO_PSP";
}
