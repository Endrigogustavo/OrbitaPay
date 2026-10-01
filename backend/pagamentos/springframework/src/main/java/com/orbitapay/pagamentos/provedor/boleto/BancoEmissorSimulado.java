package com.orbitapay.pagamentos.provedor.boleto;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.provedor.ErroDoProvedor;
import com.orbitapay.pagamentos.provedor.ProvedoresSimuladosProperties;
import com.orbitapay.pagamentos.provedor.ReferenciaSimulada;
import com.orbitapay.pagamentos.provedor.boleto.modelo.Boleto;

/** Banco emissor de boletos simulado: registra o boleto e informa se foi liquidado ou baixado por prazo. */
@Component
public class BancoEmissorSimulado {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(BRASILIA);

    private final ProvedoresSimuladosProperties.Boleto config;
    private final Clock relogio;

    public BancoEmissorSimulado(ProvedoresSimuladosProperties propriedades, Clock relogio) {
        this.config = propriedades.boleto();
        this.relogio = relogio;
    }

    public Boleto.RegistroResponse registrar(Boleto.RegistroRequest requisicao) {
        if (requisicao.valorNominalEmCentavos() <= 0) {
            throw new ErroDoProvedor(422, "VALOR_NOMINAL_INVALIDO", "valorNominalEmCentavos deve ser positivo");
        }
        Instant agora = Instant.now(relogio).truncatedTo(ChronoUnit.SECONDS);
        LocalDate vencimento;
        try {
            vencimento = LocalDate.parse(requisicao.dataDeVencimento());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ErroDoProvedor(422, "DATA_DE_VENCIMENTO_INVALIDA", "use o formato yyyy-MM-dd");
        }
        Instant fimDoVencimento = vencimento.plusDays(1).atStartOfDay(BRASILIA).toInstant();
        if (!fimDoVencimento.isAfter(agora)) {
            throw new ErroDoProvedor(422, "DATA_DE_VENCIMENTO_INVALIDA", "o vencimento já passou");
        }
        int validade = (int) Duration.between(agora, fimDoVencimento).toSeconds();
        String nossoNumero = new ReferenciaSimulada(agora, requisicao.valorNominalEmCentavos(), validade)
                .codificar("", 3);
        return new Boleto.RegistroResponse(nossoNumero, codigoDeBarras(nossoNumero, requisicao.valorNominalEmCentavos()),
                linhaDigitavel(nossoNumero, requisicao.valorNominalEmCentavos()), vencimento.toString());
    }

    public Boleto.SituacaoResponse consultar(String nossoNumero) {
        ReferenciaSimulada referencia = ReferenciaSimulada.decodificar("", nossoNumero);
        Instant agora = Instant.now(relogio);
        if (referencia.liquidadaEm(agora, config.liquidacaoSegundos())) {
            return new Boleto.SituacaoResponse(nossoNumero, Boleto.LIQUIDADO, referencia.centavos(),
                    DATA_HORA.format(referencia.liquidacao(config.liquidacaoSegundos())));
        }
        if (referencia.vencidaEm(agora)) {
            return new Boleto.SituacaoResponse(nossoNumero, Boleto.BAIXADO_POR_DECURSO_DE_PRAZO, null, null);
        }
        return new Boleto.SituacaoResponse(nossoNumero, Boleto.EM_ABERTO, null, null);
    }

    private String codigoDeBarras(String nossoNumero, long centavos) {
        String campoLivre = (nossoNumero + "0000000000000000000000000").substring(0, 25);
        return config.codigoDoBanco() + "9" + "1" + "0000" + String.format("%010d", centavos) + campoLivre;
    }

    private String linhaDigitavel(String nossoNumero, long centavos) {
        String barras = codigoDeBarras(nossoNumero, centavos);
        String campoLivre = barras.substring(19);
        return barras.substring(0, 4) + campoLivre.substring(0, 1) + "." + campoLivre.substring(1, 5) + "1 "
                + campoLivre.substring(5, 10) + "." + campoLivre.substring(10, 15) + "1 "
                + campoLivre.substring(15, 20) + "." + campoLivre.substring(20, 25) + "1 "
                + barras.charAt(4) + " " + barras.substring(5, 19);
    }
}
