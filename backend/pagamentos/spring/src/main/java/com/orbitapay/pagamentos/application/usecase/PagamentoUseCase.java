package com.orbitapay.pagamentos.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.orbitapay.pagamentos.application.exception.NaoEncontradoException;
import com.orbitapay.pagamentos.application.gateway.IEventoGateway;
import com.orbitapay.pagamentos.application.gateway.IPagamentoGateway;
import com.orbitapay.pagamentos.application.gateway.IProvedorDePagamentoGateway;
import com.orbitapay.pagamentos.entity.InstrucoesDePagamento;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.entity.SituacaoNoProvedor;
import com.orbitapay.pagamentos.service.PagamentoService;

public class PagamentoUseCase {

    private final PagamentoService pagamentoService;
    private final IPagamentoGateway pagamentoGateway;
    private final IProvedorDePagamentoGateway provedorDePagamentoGateway;
    private final IEventoGateway eventoGateway;

    public PagamentoUseCase(PagamentoService pagamentoService, IPagamentoGateway pagamentoGateway,
            IProvedorDePagamentoGateway provedorDePagamentoGateway, IEventoGateway eventoGateway) {
        this.pagamentoService = pagamentoService;
        this.pagamentoGateway = pagamentoGateway;
        this.provedorDePagamentoGateway = provedorDePagamentoGateway;
        this.eventoGateway = eventoGateway;
    }

    public Pagamento solicitarDeposito(String clienteId, BigDecimal valor, String metodoInformado) {
        MetodoDePagamento metodo = pagamentoService.converterMetodoDePagamento(metodoInformado);
        BigDecimal valorDoDeposito = pagamentoService.validarValorDoDeposito(valor, metodo);
        String pagamentoId = UUID.randomUUID().toString();
        InstrucoesDePagamento instrucoes = provedorDePagamentoGateway.emitirCobranca(pagamentoId, valorDoDeposito, metodo);
        Pagamento pagamento = pagamentoService.criarPagamentoPendente(pagamentoId, clienteId, valorDoDeposito, metodo,
                instrucoes, Instant.now());
        pagamentoGateway.salvar(pagamento);
        return pagamento;
    }

    public Pagamento buscarPagamentoDoCliente(String pagamentoId, String clienteId) {
        return pagamentoGateway.buscarPorId(pagamentoId)
                .filter(pagamento -> pagamento.clienteId().equals(clienteId))
                .orElseThrow(() -> new NaoEncontradoException("Pagamento não encontrado: " + pagamentoId));
    }

    public List<Pagamento> listarPagamentosDoCliente(String clienteId) {
        return pagamentoGateway.listarDoCliente(clienteId);
    }

    public void conciliarPagamentosPendentesComOProvedor() {
        for (Pagamento pagamento : pagamentoGateway.listarPendentes()) {
            SituacaoNoProvedor situacao = provedorDePagamentoGateway.consultarSituacaoDaCobranca(pagamento);
            if (situacao == SituacaoNoProvedor.PAGO) {
                Pagamento confirmado = pagamentoService.confirmarPagamento(pagamento, Instant.now());
                pagamentoGateway.salvar(confirmado);
                eventoGateway.publicarPagamentoConfirmado(confirmado);
            } else if (situacao == SituacaoNoProvedor.EXPIRADO) {
                pagamentoGateway.salvar(pagamentoService.expirarPagamento(pagamento, Instant.now()));
            }
        }
    }

    public void removerPagamentosDoCliente(String clienteId) {
        pagamentoGateway.removerTodosDoCliente(clienteId);
    }
}
