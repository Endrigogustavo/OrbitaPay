package com.orbitapay.ativos.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.port.out.AtivoRepository;
import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Ticker;

@Component
public class AtivoRepositoryMongoAdapter implements AtivoRepository {

    private final AtivoMongoRepository mongo;
    private final MongoTemplate template;

    public AtivoRepositoryMongoAdapter(AtivoMongoRepository mongo, MongoTemplate template) {
        this.mongo = mongo;
        this.template = template;
    }

    @Override
    public Optional<Ativo> buscar(Ticker ticker) {
        return mongo.findById(ticker.valor()).map(AtivoRepositoryMongoAdapter::paraDominio);
    }

    @Override
    public boolean existe(Ticker ticker) {
        return mongo.existsById(ticker.valor());
    }

    @Override
    public List<Ativo> listar() {
        return mongo.findAll(Sort.by("listadoEm", "ticker")).stream().map(AtivoRepositoryMongoAdapter::paraDominio)
                .toList();
    }

    @Override
    public void salvar(Ativo ativo) {
        mongo.save(paraDocumento(ativo));
    }

    @Override
    public void atualizarCotacoes(List<Ativo> ativos) {
        BulkOperations lote = template.bulkOps(BulkOperations.BulkMode.UNORDERED, AtivoDocument.class);
        ativos.forEach(ativo -> lote.updateOne(
                new Query(Criteria.where("_id").is(ativo.ticker().valor())),
                new Update().set("cotacao", ativo.cotacao()).set("historico", ativo.historico())));
        lote.execute();
    }

    @Override
    public void remover(Ticker ticker) {
        mongo.deleteById(ticker.valor());
    }

    @Override
    public boolean vazio() {
        return mongo.count() == 0;
    }

    private static AtivoDocument paraDocumento(Ativo ativo) {
        return new AtivoDocument(ativo.ticker().valor(), ativo.nome(), ativo.setor(), ativo.bolsa(), ativo.cotacao(),
                ativo.historico(), ativo.quantidadeEmitida(), ativo.listadoEm());
    }

    private static Ativo paraDominio(AtivoDocument documento) {
        return Ativo.reconstituir(new Ticker(documento.ticker()), documento.nome(), documento.setor(),
                documento.bolsa(), documento.cotacao(), documento.historico() == null ? List.of() : documento.historico(),
                documento.quantidadeEmitida(), documento.listadoEm());
    }
}
