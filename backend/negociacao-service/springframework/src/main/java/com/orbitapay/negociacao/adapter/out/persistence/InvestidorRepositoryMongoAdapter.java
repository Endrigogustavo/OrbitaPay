package com.orbitapay.negociacao.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.port.out.InvestidorRepository;
import com.orbitapay.negociacao.domain.model.Investidor;

@Component
public class InvestidorRepositoryMongoAdapter implements InvestidorRepository {

    private final InvestidorMongoRepository mongo;

    public InvestidorRepositoryMongoAdapter(InvestidorMongoRepository mongo) {
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
