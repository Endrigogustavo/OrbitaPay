package com.orbitapay.pagamentos.adapter.gateway.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.entity.InstrucoesDePagamento;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.entity.StatusDoPagamento;
import com.orbitapay.pagamentos.application.gateway.IPagamentoGateway;
import com.orbitapay.pagamentos.adapter.gateway.mongo.document.PagamentoDocument;
import com.orbitapay.pagamentos.adapter.gateway.mongo.repository.PagamentoRepository;

@Component
public class PagamentoMongoGateway implements IPagamentoGateway {

    private final PagamentoRepository pagamentoRepository;

    public PagamentoMongoGateway(PagamentoRepository pagamentoRepository) {
        this.pagamentoRepository = pagamentoRepository;
    }

    @Override
    public void salvar(Pagamento pagamento) {
        pagamentoRepository.save(converterEntidadeParaDocumento(pagamento));
    }

    @Override
    public Optional<Pagamento> buscarPorId(String pagamentoId) {
        return pagamentoRepository.findById(pagamentoId).map(PagamentoMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public List<Pagamento> listarDoCliente(String clienteId) {
        return pagamentoRepository.findByClienteIdOrderByCriadoEmDesc(clienteId).stream()
                .map(PagamentoMongoGateway::converterDocumentoParaEntidade)
                .toList();
    }

    @Override
    public List<Pagamento> listarPendentes() {
        return pagamentoRepository.findByStatus(StatusDoPagamento.PENDENTE.name()).stream()
                .map(PagamentoMongoGateway::converterDocumentoParaEntidade)
                .toList();
    }

    @Override
    public void removerTodosDoCliente(String clienteId) {
        pagamentoRepository.deleteByClienteId(clienteId);
    }

    private static PagamentoDocument converterEntidadeParaDocumento(Pagamento pagamento) {
        return new PagamentoDocument(pagamento.id(), pagamento.clienteId(), pagamento.valor(),
                pagamento.metodo().name(), pagamento.instrucoes().codigo(), pagamento.instrucoes().descricao(),
                pagamento.instrucoes().validoAte(), pagamento.status().name(), pagamento.valorPago(),
                pagamento.criadoEm(), pagamento.concluidoEm());
    }

    private static Pagamento converterDocumentoParaEntidade(PagamentoDocument documento) {
        InstrucoesDePagamento instrucoes = new InstrucoesDePagamento(documento.codigoDaCobranca(),
                documento.descricaoDaCobranca(), documento.cobrancaValidaAte());
        return new Pagamento(documento.id(), documento.clienteId(), documento.valor(),
                MetodoDePagamento.valueOf(documento.metodo()), instrucoes, StatusDoPagamento.valueOf(documento.status()),
                documento.valorPago(), documento.criadoEm(), documento.concluidoEm());
    }
}
