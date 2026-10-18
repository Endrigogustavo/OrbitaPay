package com.orbitapay.negociacao.adapter.gateway.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.entity.StatusOrdem;
import com.orbitapay.negociacao.entity.TipoOrdem;
import com.orbitapay.negociacao.application.gateway.IOrdemGateway;
import com.orbitapay.negociacao.adapter.gateway.mongo.document.OrdemDocument;
import com.orbitapay.negociacao.adapter.gateway.mongo.repository.OrdemRepository;

@Component
public class OrdemMongoGateway implements IOrdemGateway {

    private final OrdemRepository ordemRepository;

    public OrdemMongoGateway(OrdemRepository ordemRepository) {
        this.ordemRepository = ordemRepository;
    }

    @Override
    public void salvar(Ordem ordem) {
        ordemRepository.save(new OrdemDocument(ordem.id(), ordem.clienteId(), ordem.ticker(), ordem.tipo().name(),
                ordem.quantidade(), ordem.precoUnitario(), ordem.moeda(), ordem.cambio(), ordem.valorTotal(),
                ordem.status().name(), ordem.motivoRejeicao(), ordem.criadaEm(), ordem.atualizadaEm()));
    }

    @Override
    public Optional<Ordem> buscarPorId(String ordemId) {
        return ordemRepository.findById(ordemId).map(OrdemMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public List<Ordem> listarDoCliente(String clienteId) {
        return ordemRepository.findByClienteIdOrderByCriadaEmDesc(clienteId).stream()
                .map(OrdemMongoGateway::converterDocumentoParaEntidade)
                .toList();
    }

    private static Ordem converterDocumentoParaEntidade(OrdemDocument documento) {
        return new Ordem(documento.id(), documento.clienteId(), documento.ticker(), TipoOrdem.valueOf(documento.tipo()),
                documento.quantidade(), documento.precoUnitario(), documento.moeda(), documento.cambio(),
                documento.valorTotal(), StatusOrdem.valueOf(documento.status()), documento.motivoRejeicao(),
                documento.criadaEm(), documento.atualizadaEm());
    }
}
