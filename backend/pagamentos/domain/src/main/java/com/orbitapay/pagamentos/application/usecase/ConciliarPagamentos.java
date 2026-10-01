package com.orbitapay.pagamentos.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.pagamentos.application.dto.SituacaoDaCobranca;
import com.orbitapay.pagamentos.application.service.ProvedorDePagamentos;
import com.orbitapay.pagamentos.application.service.ProvedorIndisponivelException;
import com.orbitapay.pagamentos.application.service.PublicadorDeEventosDePagamento;
import com.orbitapay.pagamentos.domain.event.PagamentoConfirmado;
import com.orbitapay.pagamentos.domain.event.PagamentoExpirado;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.domain.repository.PagamentoRepository;

/**
 * Pergunta aos provedores, pela fachada, a situação das cobranças pendentes e conclui as que foram pagas ou
 * expiraram. Cada conclusão vira um evento: {@code pagamento.confirmado} faz o contexto de Contas creditar o
 * depósito.
 */
public class ConciliarPagamentos {

    public record Resultado(int verificados, int confirmados, int expirados, int falhas) {
    }

    private final PagamentoRepository repositorio;
    private final ProvedorDePagamentos provedores;
    private final PublicadorDeEventosDePagamento publicador;
    private final Clock relogio;
    private final int lote;

    public ConciliarPagamentos(PagamentoRepository repositorio, ProvedorDePagamentos provedores,
            PublicadorDeEventosDePagamento publicador, Clock relogio, int lote) {
        this.repositorio = repositorio;
        this.provedores = provedores;
        this.publicador = publicador;
        this.relogio = relogio;
        this.lote = lote;
    }

    public Resultado executar() {
        int verificados = 0;
        int confirmados = 0;
        int expirados = 0;
        int falhas = 0;
        for (Pagamento pagamento : repositorio.listarPendentes(lote)) {
            verificados++;
            SituacaoDaCobranca situacao;
            try {
                situacao = provedores.consultar(pagamento.metodo(), pagamento.referenciaExterna());
            } catch (ProvedorIndisponivelException e) {
                falhas++;
                continue;
            }
            Instant agora = Instant.now(relogio);
            switch (situacao.estado()) {
                case PAGA -> {
                    if (pagamento.confirmar(situacao.valorPago(), situacao.pagaEm() == null ? agora : situacao.pagaEm())) {
                        repositorio.salvar(pagamento);
                        publicador.publicar(new PagamentoConfirmado(pagamento, agora));
                        confirmados++;
                    }
                }
                case EXPIRADA -> {
                    if (pagamento.expirar(agora)) {
                        repositorio.salvar(pagamento);
                        publicador.publicar(new PagamentoExpirado(pagamento, agora));
                        expirados++;
                    }
                }
                case AGUARDANDO -> {
                }
            }
        }
        return new Resultado(verificados, confirmados, expirados, falhas);
    }
}
