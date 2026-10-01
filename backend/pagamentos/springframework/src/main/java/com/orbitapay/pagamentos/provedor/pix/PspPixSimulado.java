package com.orbitapay.pagamentos.provedor.pix;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.provedor.ErroDoProvedor;
import com.orbitapay.pagamentos.provedor.ProvedoresSimuladosProperties;
import com.orbitapay.pagamentos.provedor.ReferenciaSimulada;
import com.orbitapay.pagamentos.provedor.pix.modelo.CobrancaImediata;

/**
 * PSP Pix simulado. Imita o contrato da API Pix do Banco Central: {@code POST /cob} cria a cobrança e o PSP
 * gera o {@code txid}; {@code GET /cob/{txid}} devolve o status e, quando paga, a lista {@code pix} recebida.
 */
@Component
public class PspPixSimulado {

    private static final String PREFIXO_TXID = "ORB";
    private static final DateTimeFormatter COMPACTO = DateTimeFormatter.ofPattern("yyyyMMddHHmm").withZone(ZoneOffset.UTC);

    private final ProvedoresSimuladosProperties.Pix config;
    private final Clock relogio;

    public PspPixSimulado(ProvedoresSimuladosProperties propriedades, Clock relogio) {
        this.config = propriedades.pix();
        this.relogio = relogio;
    }

    public CobrancaImediata.Resposta criarCobranca(CobrancaImediata.Requisicao requisicao) {
        if (requisicao.valor() == null || requisicao.valor().original() == null
                || !requisicao.valor().original().matches("\\d{1,10}\\.\\d{2}")) {
            throw new ErroDoProvedor(400, "CobValorInvalido", "valor.original deve ter duas casas decimais");
        }
        if (requisicao.chave() == null || requisicao.chave().isBlank()) {
            throw new ErroDoProvedor(400, "CobChaveInvalida", "chave do recebedor obrigatória");
        }
        int expiracao = requisicao.calendario() == null || requisicao.calendario().expiracao() == null ? 3600
                : requisicao.calendario().expiracao();
        long centavos = new BigDecimal(requisicao.valor().original()).movePointRight(2).longValueExact();
        Instant agora = Instant.now(relogio).truncatedTo(ChronoUnit.SECONDS);
        String txid = new ReferenciaSimulada(agora, centavos, expiracao).codificar(PREFIXO_TXID, 5);
        return resposta(txid, requisicao.chave(), agora);
    }

    public CobrancaImediata.Resposta consultarCobranca(String txid, String chave) {
        return resposta(txid, chave, Instant.now(relogio));
    }

    private CobrancaImediata.Resposta resposta(String txid, String chave, Instant agora) {
        ReferenciaSimulada referencia = ReferenciaSimulada.decodificar(PREFIXO_TXID, txid);
        String valor = BigDecimal.valueOf(referencia.centavos(), 2).toPlainString();
        CobrancaImediata.Calendario calendario = new CobrancaImediata.Calendario(referencia.emitidaEm().toString(),
                referencia.validadeSegundos());
        String status;
        List<CobrancaImediata.PixRecebido> recebidos = List.of();
        if (referencia.liquidadaEm(agora, config.liquidacaoSegundos())) {
            Instant horario = referencia.liquidacao(config.liquidacaoSegundos());
            status = CobrancaImediata.CONCLUIDA;
            recebidos = List.of(new CobrancaImediata.PixRecebido("E" + config.ispb() + COMPACTO.format(horario)
                    + txid.substring(txid.length() - 11), txid, valor, horario.toString()));
        } else if (referencia.vencidaEm(agora)) {
            status = CobrancaImediata.REMOVIDA_PELO_PSP;
        } else {
            status = CobrancaImediata.ATIVA;
        }
        return new CobrancaImediata.Resposta(txid, 0, status, calendario, new CobrancaImediata.Valor(valor), chave,
                brCode(chave, valor, txid), recebidos);
    }

    /** "Pix copia e cola" no formato EMV (BR Code), com CRC16 no final. */
    private static String brCode(String chave, String valor, String txid) {
        String contaRecebedor = campo("00", "br.gov.bcb.pix") + campo("01", chave);
        String semCrc = campo("00", "01") + campo("26", contaRecebedor) + campo("52", "0000") + campo("53", "986")
                + campo("54", valor) + campo("58", "BR") + campo("59", "ORBITAPAY SA") + campo("60", "SAO PAULO")
                + campo("62", campo("05", txid.substring(0, Math.min(25, txid.length())))) + "6304";
        return semCrc + String.format("%04X", crc16(semCrc.getBytes(StandardCharsets.US_ASCII)));
    }

    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    private static int crc16(byte[] dados) {
        int crc = 0xFFFF;
        for (byte b : dados) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
            }
        }
        return crc & 0xFFFF;
    }
}
