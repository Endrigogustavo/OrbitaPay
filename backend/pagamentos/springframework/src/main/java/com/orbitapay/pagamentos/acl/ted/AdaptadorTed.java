package com.orbitapay.pagamentos.acl.ted;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.acl.AclProperties;
import com.orbitapay.pagamentos.acl.AdaptadorDeProvedor;
import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.application.service.ProvedorIndisponivelException;
import com.orbitapay.pagamentos.domain.model.Dinheiro;
import com.orbitapay.pagamentos.domain.model.InstrucoesDePagamento;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;
import com.orbitapay.pagamentos.provedor.ErroDoProvedor;
import com.orbitapay.pagamentos.provedor.ted.SpbSimulado;
import com.orbitapay.pagamentos.provedor.ted.modelo.MovimentoSpb;

/**
 * Traduz entre o OrbitaPay e o SPB: {@link Dinheiro} vira texto "1.234,56", a situação numérica (0, 1, 9) vira
 * aguardando/paga/expirada e as datas {@code dd/MM/yyyy HH:mm:ss} de Brasília viram {@link Instant}.
 */
@Component
public class AdaptadorTed implements AdaptadorDeProvedor {

    private static final String NOME = "SPB · TED (simulado)";
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final SpbSimulado spb;
    private final AclProperties.Ted config;

    public AdaptadorTed(SpbSimulado spb, AclProperties propriedades) {
        this.spb = spb;
        this.config = propriedades.ted();
    }

    @Override
    public MetodoDePagamento metodo() {
        return MetodoDePagamento.TED;
    }

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao) {
        MovimentoSpb.PedidoDeIdentificador pedido = new MovimentoSpb.PedidoDeIdentificador(solicitacao.pagamentoId(),
                formato().format(solicitacao.valor().valor()), config.horasDeValidade());
        MovimentoSpb.Identificador identificador = chamar(() -> spb.gerarIdentificador(pedido));
        String dadosDaConta = "Banco " + identificador.bancoFavorecido() + " · Ag " + identificador.agenciaFavorecida()
                + " · Conta " + identificador.contaFavorecida() + " · Id " + identificador.codigoIdentificador();
        return new CobrancaEmitida(NOME, identificador.codigoIdentificador(), new InstrucoesDePagamento(dadosDaConta,
                "TED para " + identificador.nomeFavorecido() + " informando o identificador",
                paraInstante(identificador.validoAte())));
    }

    @Override
    public SituacaoDaCobranca consultar(String codigoIdentificador) {
        MovimentoSpb.Movimento movimento = chamar(() -> spb.consultarMovimento(codigoIdentificador));
        return switch (movimento.codigoSituacao()) {
            case MovimentoSpb.AGUARDANDO_CREDITO -> SituacaoDaCobranca.aguardando();
            case MovimentoSpb.CREDITADO -> SituacaoDaCobranca.paga(lerValor(movimento.valorCreditado()),
                    paraInstante(movimento.dataHoraDoCredito()));
            case MovimentoSpb.CANCELADO -> SituacaoDaCobranca.expirada();
            default -> throw new ProvedorIndisponivelException(NOME, "situação " + movimento.codigoSituacao());
        };
    }

    private static Instant paraInstante(String dataHora) {
        return LocalDateTime.parse(dataHora, DATA_HORA).atZone(BRASILIA).toInstant();
    }

    private static Dinheiro lerValor(String valor) {
        try {
            return new Dinheiro((BigDecimal) formato().parse(valor));
        } catch (ParseException e) {
            throw new ProvedorIndisponivelException(NOME, "valor creditado ilegível: " + valor);
        }
    }

    private static DecimalFormat formato() {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR")));
        formato.setParseBigDecimal(true);
        return formato;
    }

    private static <T> T chamar(Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (ErroDoProvedor e) {
            throw new ProvedorIndisponivelException(NOME, e.codigo() + " · " + e.getMessage());
        }
    }
}
