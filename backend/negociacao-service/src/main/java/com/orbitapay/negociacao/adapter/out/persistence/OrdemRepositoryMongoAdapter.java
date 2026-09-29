package com.orbitapay.negociacao.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.port.out.OrdemRepository;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.model.StatusOrdem;
import com.orbitapay.negociacao.domain.model.TipoOrdem;

@Component
public class OrdemRepositoryMongoAdapter implements OrdemRepository {

    private final OrdemMongoRepository mongo;

    public OrdemRepositoryMongoAdapter(OrdemMongoRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public String proximoId() {
        return new ObjectId().toHexString();
    }

    @Override
    public void salvar(Ordem ordem) {
        mongo.save(new OrdemDocument(ordem.id(), ordem.clienteId(), ordem.ticker(), ordem.tipo().name(),
                ordem.quantidade(), ordem.precoUnitario(), ordem.moeda(), ordem.cambio(), ordem.valorTotal(),
                ordem.status().name(), ordem.motivoRejeicao(), ordem.criadaEm(), ordem.atualizadaEm()));
    }

    @Override
    public Optional<Ordem> buscar(String ordemId) {
        return mongo.findById(ordemId).map(OrdemRepositoryMongoAdapter::paraDominio);
    }

    @Override
    public List<Ordem> listarDoCliente(String clienteId) {
        return mongo.findByClienteIdOrderByCriadaEmDesc(clienteId).stream()
                .map(OrdemRepositoryMongoAdapter::paraDominio).toList();
    }

    private static Ordem paraDominio(OrdemDocument documento) {
        return Ordem.reconstituir(documento.id(), documento.clienteId(), documento.ticker(),
                TipoOrdem.valueOf(documento.tipo()), documento.quantidade(), documento.precoUnitario(),
                documento.moeda(), documento.cambio(), documento.valorTotal(), StatusOrdem.valueOf(documento.status()),
                documento.motivoRejeicao(), documento.criadaEm(), documento.atualizadaEm());
    }
}
