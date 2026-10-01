package com.orbitapay.negociacao.persistence;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.exception.TravaIndisponivelException;
import com.orbitapay.negociacao.domain.exception.RecursoNaoEncontradoException;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.persistence.trava.TravaPessimistaMongo;

@Component
public class MongoAtivoNegociavelRepository implements AtivoNegociavelRepository {

    private final AtivoNegociavelDocumentRepository mongo;
    private final MongoTemplate template;
    private final TravaPessimistaMongo trava;

    public MongoAtivoNegociavelRepository(AtivoNegociavelDocumentRepository mongo, MongoTemplate template,
            TravaPessimistaMongo trava) {
        this.mongo = mongo;
        this.template = template;
        this.trava = trava;
    }

    @Override
    public Optional<AtivoNegociavel> buscar(String ticker) {
        return mongo.findById(ticker).map(MongoAtivoNegociavelRepository::paraDominio);
    }

    @Override
    public boolean existe(String ticker) {
        return mongo.existsById(ticker);
    }

    @Override
    public void inserir(AtivoNegociavel ativo) {
        mongo.insert(paraDocumento(ativo));
    }

    @Override
    public AtivoTravado travar(String ticker) {
        String dono = TravaPessimistaMongo.novoDono();
        AtivoNegociavelDocument documento = trava.adquirir(AtivoNegociavelDocument.class, porTicker(ticker),
                recurso(ticker), dono);
        if (documento == null) {
            throw new RecursoNaoEncontradoException("Ativo não negociado: " + ticker);
        }
        return new AtivoTravado(paraDominio(documento), dono);
    }

    @Override
    public void salvarELiberar(AtivoTravado travado) {
        String ticker = travado.ativo().ticker();
        if (!trava.substituirELiberar(AtivoNegociavelDocument.class, porTicker(ticker), recurso(ticker),
                travado.dono(), paraDocumento(travado.ativo()))) {
            throw new TravaIndisponivelException(recurso(ticker));
        }
    }

    @Override
    public void liberar(AtivoTravado travado) {
        String ticker = travado.ativo().ticker();
        trava.liberar(AtivoNegociavelDocument.class, porTicker(ticker), recurso(ticker), travado.dono());
    }

    @Override
    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        if (cotacoes.isEmpty()) {
            return;
        }
        BulkOperations lote = template.bulkOps(BulkOperations.BulkMode.UNORDERED, AtivoNegociavelDocument.class);
        cotacoes.forEach((ticker, cotacao) -> lote.updateOne(new Query(porTicker(ticker)),
                new Update().set("cotacao", cotacao)));
        lote.execute();
    }

    private static Criteria porTicker(String ticker) {
        return Criteria.where("_id").is(ticker);
    }

    private static String recurso(String ticker) {
        return "ativo:" + ticker;
    }

    private static AtivoNegociavelDocument paraDocumento(AtivoNegociavel ativo) {
        return new AtivoNegociavelDocument(ativo.ticker(), ativo.nome(), ativo.bolsa(), ativo.moeda(), ativo.cambio(),
                ativo.cotacao(), ativo.quantidadeEmitida(), ativo.quantidadeDisponivel(),
                ativo.quantidadeReservada(), ativo.negociavel(), null);
    }

    private static AtivoNegociavel paraDominio(AtivoNegociavelDocument documento) {
        return AtivoNegociavel.reconstituir(documento.ticker(), documento.nome(), documento.bolsa(),
                documento.moeda(), documento.cambio(), documento.cotacao(), documento.quantidadeEmitida(),
                documento.quantidadeDisponivel(), documento.quantidadeReservada(), documento.negociavel());
    }
}
