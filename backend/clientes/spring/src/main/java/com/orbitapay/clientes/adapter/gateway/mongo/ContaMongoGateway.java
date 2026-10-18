package com.orbitapay.clientes.adapter.gateway.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.clientes.entity.Conta;
import com.orbitapay.clientes.entity.Lancamento;
import com.orbitapay.clientes.entity.TipoLancamento;
import com.orbitapay.clientes.application.gateway.IContaGateway;
import com.orbitapay.clientes.adapter.gateway.mongo.document.ContaDocument;
import com.orbitapay.clientes.adapter.gateway.mongo.document.LancamentoDocument;
import com.orbitapay.clientes.adapter.gateway.mongo.repository.ContaRepository;

@Component
public class ContaMongoGateway implements IContaGateway {

    private final ContaRepository contaRepository;

    public ContaMongoGateway(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    public void salvar(Conta conta) {
        List<LancamentoDocument> lancamentos = conta.lancamentos().stream()
                .map(lancamento -> new LancamentoDocument(lancamento.id(), lancamento.tipo().name(), lancamento.valor(),
                        lancamento.descricao(), lancamento.referencia(), lancamento.ocorridoEm()))
                .toList();
        contaRepository.save(new ContaDocument(conta.id(), conta.clienteId(), conta.agencia(), conta.numero(),
                conta.saldo(), lancamentos, conta.abertaEm()));
    }

    @Override
    public Optional<Conta> buscarPorCliente(String clienteId) {
        return contaRepository.findByClienteId(clienteId).map(ContaMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public List<Conta> listarTodas() {
        return contaRepository.findAll().stream().map(ContaMongoGateway::converterDocumentoParaEntidade).toList();
    }

    @Override
    public void removerPorCliente(String clienteId) {
        contaRepository.deleteByClienteId(clienteId);
    }

    private static Conta converterDocumentoParaEntidade(ContaDocument documento) {
        List<Lancamento> lancamentos = documento.lancamentos().stream()
                .map(lancamento -> new Lancamento(lancamento.id(), TipoLancamento.valueOf(lancamento.tipo()),
                        lancamento.valor(), lancamento.descricao(), lancamento.referencia(), lancamento.ocorridoEm()))
                .toList();
        return new Conta(documento.id(), documento.clienteId(), documento.agencia(), documento.numero(),
                documento.saldo(), lancamentos, documento.abertaEm());
    }
}
