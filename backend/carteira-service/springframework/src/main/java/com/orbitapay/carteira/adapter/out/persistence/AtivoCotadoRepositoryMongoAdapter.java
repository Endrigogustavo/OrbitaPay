package com.orbitapay.carteira.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.port.out.AtivoCotadoRepository;
import com.orbitapay.carteira.domain.model.AtivoCotado;

@Component
public class AtivoCotadoRepositoryMongoAdapter implements AtivoCotadoRepository {

    private final AtivoCotadoMongoRepository mongo;
    private final MongoTemplate template;

    public AtivoCotadoRepositoryMongoAdapter(AtivoCotadoMongoRepository mongo, MongoTemplate template) {
        this.mongo = mongo;
        this.template = template;
    }

    @Override
    public Map<String, AtivoCotado> buscarTodos(Collection<String> tickers) {
        return StreamSupport.stream(mongo.findAllById(tickers).spliterator(), false)
                .map(d -> new AtivoCotado(d.ticker(), d.nome(), d.moeda(), d.cambio(), d.cotacao()))
                .collect(Collectors.toMap(AtivoCotado::ticker, Function.identity()));
    }

    @Override
    public void salvar(AtivoCotado ativo) {
        mongo.save(new AtivoCotadoDocument(ativo.ticker(), ativo.nome(), ativo.moeda(), ativo.cambio(), ativo.cotacao()));
    }

    @Override
    public void remover(String ticker) {
        mongo.deleteById(ticker);
    }

    @Override
    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        if (cotacoes.isEmpty()) {
            return;
        }
        BulkOperations lote = template.bulkOps(BulkOperations.BulkMode.UNORDERED, AtivoCotadoDocument.class);
        cotacoes.forEach((ticker, cotacao) -> lote.updateOne(new Query(Criteria.where("_id").is(ticker)),
                new Update().set("cotacao", cotacao)));
        lote.execute();
    }
}
