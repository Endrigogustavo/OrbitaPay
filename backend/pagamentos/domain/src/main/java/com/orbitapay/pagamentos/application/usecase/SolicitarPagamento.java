package com.orbitapay.pagamentos.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.pagamentos.application.dto.CobrancaEmitida;
import com.orbitapay.pagamentos.application.dto.SolicitacaoDeCobranca;
import com.orbitapay.pagamentos.application.service.ProvedorDePagamentos;
import com.orbitapay.pagamentos.domain.model.Dinheiro;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.domain.repository.PagamentoRepository;

/** Emite a cobrança no provedor do método escolhido e registra o pagamento como pendente. */
public class SolicitarPagamento {

    public record Comando(String clienteId, BigDecimal valor, String metodo) {
    }

    private final PagamentoRepository repositorio;
    private final ProvedorDePagamentos provedores;
    private final Clock relogio;

    public SolicitarPagamento(PagamentoRepository repositorio, ProvedorDePagamentos provedores, Clock relogio) {
        this.repositorio = repositorio;
        this.provedores = provedores;
        this.relogio = relogio;
    }

    public Pagamento executar(Comando comando) {
        MetodoDePagamento metodo = MetodoDePagamento.de(comando.metodo());
        Dinheiro valor = new Dinheiro(comando.valor());
        Pagamento.validarSolicitacao(valor, metodo);
        String id = repositorio.proximoId();
        CobrancaEmitida cobranca = provedores.emitir(new SolicitacaoDeCobranca(id, comando.clienteId(), valor, metodo));
        Pagamento pagamento = Pagamento.emitido(id, comando.clienteId(), valor, metodo, cobranca.provedor(),
                cobranca.referenciaExterna(), cobranca.instrucoes(), Instant.now(relogio));
        repositorio.salvar(pagamento);
        return pagamento;
    }
}
