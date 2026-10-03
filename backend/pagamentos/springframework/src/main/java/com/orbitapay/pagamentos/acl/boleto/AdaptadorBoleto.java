package com.orbitapay.pagamentos.acl.boleto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
import com.orbitapay.pagamentos.provedor.boleto.BancoEmissorSimulado;
import com.orbitapay.pagamentos.provedor.boleto.modelo.Boleto;

@Component
public class AdaptadorBoleto implements AdaptadorDeProvedor {

    private static final String NOME = "Banco emissor de boletos (simulado)";
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BancoEmissorSimulado banco;
    private final AclProperties.Boleto config;
    private final Clock relogio;

    public AdaptadorBoleto(BancoEmissorSimulado banco, AclProperties propriedades, Clock relogio) {
        this.banco = banco;
        this.config = propriedades.boleto();
        this.relogio = relogio;
    }

    @Override
    public MetodoDePagamento metodo() {
        return MetodoDePagamento.BOLETO;
    }

    @Override
    public String nome() {
        return NOME;
    }

    @Override
    public CobrancaEmitida emitir(SolicitacaoDeCobranca solicitacao) {
        LocalDate vencimento = LocalDate.now(relogio.withZone(BRASILIA)).plusDays(config.diasParaVencimento());
        Boleto.RegistroRequest requisicao = new Boleto.RegistroRequest(solicitacao.pagamentoId(),
                solicitacao.valor().emCentavos(), vencimento.toString(), solicitacao.clienteId());
        Boleto.RegistroResponse resposta;
        try {
            resposta = banco.registrar(requisicao);
        } catch (ErroDoProvedor e) {
            throw falhaDoProvedor(e);
        }
        LocalDate dataDeVencimento = LocalDate.parse(resposta.dataDeVencimento());
        return new CobrancaEmitida(NOME, resposta.nossoNumero(), new InstrucoesDePagamento(resposta.linhaDigitavel(),
                "Linha digitável · vence em " + DATA_BR.format(dataDeVencimento),
                dataDeVencimento.plusDays(1).atStartOfDay(BRASILIA).toInstant()));
    }

    @Override
    public SituacaoDaCobranca consultar(String nossoNumero) {
        Boleto.SituacaoResponse resposta;
        try {
            resposta = banco.consultar(nossoNumero);
        } catch (ErroDoProvedor e) {
            throw falhaDoProvedor(e);
        }
        return switch (resposta.situacao()) {
            case Boleto.EM_ABERTO -> SituacaoDaCobranca.aguardando();
            case Boleto.LIQUIDADO -> SituacaoDaCobranca.paga(Dinheiro.deCentavos(resposta.valorPagoEmCentavos()),
                    LocalDateTime.parse(resposta.dataHoraDaLiquidacao(), DATA_HORA).atZone(BRASILIA).toInstant());
            case Boleto.BAIXADO_POR_DECURSO_DE_PRAZO -> SituacaoDaCobranca.expirada();
            default -> throw new ProvedorIndisponivelException(NOME, "situação desconhecida " + resposta.situacao());
        };
    }

    private static ProvedorIndisponivelException falhaDoProvedor(ErroDoProvedor e) {
        return new ProvedorIndisponivelException(NOME, e.codigo() + " · " + e.getMessage());
    }
}
