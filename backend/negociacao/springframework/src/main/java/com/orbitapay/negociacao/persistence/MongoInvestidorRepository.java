package com.orbitapay.negociacao.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.domain.model.Investidor;
import com.orbitapay.negociacao.domain.repository.InvestidorRepository;

@Component
public class MongoInvestidorRepository implements InvestidorRepository {

    private final InvestidorDocumentRepository mongo;

    public MongoInvestidorRepository(InvestidorDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Investidor> buscar(String clienteId) {
        return mongo.findById(clienteId).map(d -> new Investidor(d.clienteId(), d.bloqueado()));
    }

    @Override
    public void salvar(Investidor investidor) {
        mongo.save(new InvestidorDocument(investidor.clienteId(), investidor.bloqueado()));
    }

    @Override
    public void remover(String clienteId) {
        mongo.deleteById(clienteId);
    }
}
