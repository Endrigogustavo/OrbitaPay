package com.orbitapay.ativos.adapter.gateway.mongo;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.entity.Ativo;
import com.orbitapay.ativos.application.gateway.IAtivoGateway;
import com.orbitapay.ativos.adapter.gateway.mongo.document.AtivoDocument;
import com.orbitapay.ativos.adapter.gateway.mongo.repository.AtivoRepository;

@Component
public class AtivoMongoGateway implements IAtivoGateway {

    private final AtivoRepository ativoRepository;
    private final MongoTemplate mongoTemplate;

    public AtivoMongoGateway(AtivoRepository ativoRepository, MongoTemplate mongoTemplate) {
        this.ativoRepository = ativoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void salvar(Ativo ativo) {
        ativoRepository.save(converterEntidadeParaDocumento(ativo));
    }

    @Override
    public Optional<Ativo> buscarPorTicker(String ticker) {
        return ativoRepository.findById(ticker).map(AtivoMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public List<Ativo> listarTodos() {
        return ativoRepository.findAll().stream().map(AtivoMongoGateway::converterDocumentoParaEntidade).toList();
    }

    @Override
    public void remover(String ticker) {
        ativoRepository.deleteById(ticker);
    }

    @Override
    public boolean retirarDoEstoqueSeHouverQuantidade(String ticker, long quantidade) {
        long documentosAlterados = mongoTemplate.updateFirst(
                query(where("_id").is(ticker).and("quantidadeDisponivel").gte(quantidade)),
                new Update().inc("quantidadeDisponivel", -quantidade),
                AtivoDocument.class).getModifiedCount();
        return documentosAlterados == 1;
    }

    @Override
    public void devolverAoEstoque(String ticker, long quantidade) {
        mongoTemplate.updateFirst(query(where("_id").is(ticker)), new Update().inc("quantidadeDisponivel", quantidade),
                AtivoDocument.class);
    }

    @Override
    public void atualizarCotacao(String ticker, BigDecimal novaCotacao, List<BigDecimal> historicoAtualizado) {
        mongoTemplate.updateFirst(query(where("_id").is(ticker)),
                new Update().set("cotacao", novaCotacao).set("historico", historicoAtualizado), AtivoDocument.class);
    }

    private static AtivoDocument converterEntidadeParaDocumento(Ativo ativo) {
        return new AtivoDocument(ativo.ticker(), ativo.nome(), ativo.setor(), ativo.bolsa(), ativo.cotacao(),
                ativo.historico(), ativo.quantidadeEmitida(), ativo.quantidadeDisponivel(), ativo.listadoEm());
    }

    private static Ativo converterDocumentoParaEntidade(AtivoDocument documento) {
        return new Ativo(documento.ticker(), documento.nome(), documento.setor(), documento.bolsa(),
                documento.cotacao(), documento.historico(), documento.quantidadeEmitida(),
                documento.quantidadeDisponivel(), documento.listadoEm());
    }
}
