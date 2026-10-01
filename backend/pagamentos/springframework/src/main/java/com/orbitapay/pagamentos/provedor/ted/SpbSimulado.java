package com.orbitapay.pagamentos.provedor.ted;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.provedor.ErroDoProvedor;
import com.orbitapay.pagamentos.provedor.ProvedoresSimuladosProperties;
import com.orbitapay.pagamentos.provedor.ReferenciaSimulada;
import com.orbitapay.pagamentos.provedor.ted.modelo.MovimentoSpb;

/** Banco liquidante simulado no SPB: gera identificadores de depósito por TED e informa os créditos recebidos. */
@Component
public class SpbSimulado {

    private static final String PREFIXO = "ID";
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(BRASILIA);

    private final ProvedoresSimuladosProperties.Ted config;
    private final Clock relogio;

    public SpbSimulado(ProvedoresSimuladosProperties propriedades, Clock relogio) {
        this.config = propriedades.ted();
        this.relogio = relogio;
    }

    public MovimentoSpb.Identificador gerarIdentificador(MovimentoSpb.PedidoDeIdentificador pedido) {
        long centavos = lerValor(pedido.valorEsperado());
        if (centavos <= 0 || pedido.horasDeValidade() <= 0) {
            throw new ErroDoProvedor(400, "SPB-014", "Valor esperado ou validade inválidos");
        }
        Instant agora = Instant.now(relogio).truncatedTo(ChronoUnit.SECONDS);
        ReferenciaSimulada referencia = new ReferenciaSimulada(agora, centavos, pedido.horasDeValidade() * 3600);
        return new MovimentoSpb.Identificador(referencia.codificar(PREFIXO, 4), config.bancoFavorecido(),
                config.agenciaFavorecida(), config.contaFavorecida(), config.nomeFavorecido(),
                pedido.valorEsperado(), DATA_HORA.format(referencia.validaAte()));
    }

    public MovimentoSpb.Movimento consultarMovimento(String codigoIdentificador) {
        ReferenciaSimulada referencia = ReferenciaSimulada.decodificar(PREFIXO, codigoIdentificador);
        Instant agora = Instant.now(relogio);
        if (referencia.liquidadaEm(agora, config.liquidacaoSegundos())) {
            return new MovimentoSpb.Movimento(codigoIdentificador, MovimentoSpb.CREDITADO,
                    formatarValor(referencia.centavos()),
                    DATA_HORA.format(referencia.liquidacao(config.liquidacaoSegundos())));
        }
        if (referencia.vencidaEm(agora)) {
            return new MovimentoSpb.Movimento(codigoIdentificador, MovimentoSpb.CANCELADO, null, null);
        }
        return new MovimentoSpb.Movimento(codigoIdentificador, MovimentoSpb.AGUARDANDO_CREDITO, null, null);
    }

    private static DecimalFormat formato() {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR")));
        formato.setParseBigDecimal(true);
        return formato;
    }

    private static long lerValor(String valor) {
        try {
            return ((BigDecimal) formato().parse(valor)).movePointRight(2).longValueExact();
        } catch (ParseException | ArithmeticException | NullPointerException e) {
            throw new ErroDoProvedor(400, "SPB-011", "Valor esperado em formato inválido: " + valor);
        }
    }

    private static String formatarValor(long centavos) {
        return formato().format(BigDecimal.valueOf(centavos, 2));
    }
}
