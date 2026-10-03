package com.orbitapay.pagamentos.persistence;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.domain.model.Dinheiro;
import com.orbitapay.pagamentos.domain.model.InstrucoesDePagamento;
import com.orbitapay.pagamentos.domain.model.MetodoDePagamento;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.domain.model.StatusDoPagamento;
import com.orbitapay.pagamentos.domain.repository.PagamentoRepository;

@Component
public class MongoPagamentoRepository implements PagamentoRepository {

    private final PagamentoDocumentRepository mongo;

    public MongoPagamentoRepository(PagamentoDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public String proximoId() {
        return new ObjectId().toHexString();
    }

    @Override
    public void salvar(Pagamento pagamento) {
        mongo.save(new PagamentoDocument(pagamento.id(), pagamento.clienteId(), pagamento.valor().valor(),
                pagamento.metodo().name(), pagamento.provedor(), pagamento.referenciaExterna(),
                pagamento.instrucoes().codigo(), pagamento.instrucoes().descricao(), pagamento.instrucoes().validoAte(),
                pagamento.status().name(), pagamento.valorPago() == null ? null : pagamento.valorPago().valor(),
                pagamento.criadoEm(), pagamento.concluidoEm()));
    }

    @Override
    public Optional<Pagamento> buscar(String pagamentoId) {
        return mongo.findById(pagamentoId).map(MongoPagamentoRepository::paraDominio);
    }

    @Override
    public List<Pagamento> listarDoCliente(String clienteId) {
        return mongo.findByClienteIdOrderByCriadoEmDesc(clienteId).stream()
                .map(MongoPagamentoRepository::paraDominio).toList();
    }

    @Override
    public List<Pagamento> listarPendentes(int limite) {
        return mongo.findByStatusOrderByCriadoEmAsc(StatusDoPagamento.PENDENTE.name(), PageRequest.of(0, limite))
                .stream().map(MongoPagamentoRepository::paraDominio).toList();
    }

    private static Pagamento paraDominio(PagamentoDocument documento) {
        return new Pagamento(documento.id(), documento.clienteId(), new Dinheiro(documento.valor()),
                MetodoDePagamento.valueOf(documento.metodo()), documento.provedor(), documento.referenciaExterna(),
                new InstrucoesDePagamento(documento.codigo(), documento.descricao(), documento.validoAte()),
                StatusDoPagamento.valueOf(documento.status()),
                documento.valorPago() == null ? null : new Dinheiro(documento.valorPago()), documento.criadoEm(),
                documento.concluidoEm());
    }
}
