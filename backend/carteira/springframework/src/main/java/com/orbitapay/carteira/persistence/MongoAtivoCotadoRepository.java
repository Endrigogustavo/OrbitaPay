package com.orbitapay.carteira.persistence;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.domain.model.AtivoCotado;
import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;

@Component
public class MongoAtivoCotadoRepository implements AtivoCotadoRepository {

    private final AtivoCotadoDocumentRepository mongo;
    private final MongoTemplate template;

    public MongoAtivoCotadoRepository(AtivoCotadoDocumentRepository mongo, MongoTemplate template) {
        this.mongo = mongo;
        this.template = template;
    }

    @Override
    public Map<String, AtivoCotado> buscarTodos(Collection<String> tickers) {
        Map<String, AtivoCotado> ativos = new HashMap<>();
        for (AtivoCotadoDocument d : mongo.findAllById(tickers)) {
            ativos.put(d.ticker(), new AtivoCotado(d.ticker(), d.nome(), d.moeda(), d.cambio(), d.cotacao()));
        }
        return ativos;
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
        for (Map.Entry<String, BigDecimal> cotacao : cotacoes.entrySet()) {
            Query porTicker = new Query(Criteria.where("_id").is(cotacao.getKey()));
            template.updateFirst(porTicker, new Update().set("cotacao", cotacao.getValue()), AtivoCotadoDocument.class);
        }
    }
}
