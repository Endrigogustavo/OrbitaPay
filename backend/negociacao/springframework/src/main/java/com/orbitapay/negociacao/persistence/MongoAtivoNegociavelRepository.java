package com.orbitapay.negociacao.persistence;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.AtivoTravado;
import com.orbitapay.negociacao.domain.repository.TravaDeAtivo;
import com.orbitapay.negociacao.persistence.trava.TravaPessimistaMongo;

@Component
public class MongoAtivoNegociavelRepository implements AtivoNegociavelRepository, TravaDeAtivo {

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
        String dono = UUID.randomUUID().toString();
        AtivoNegociavelDocument documento = trava.travar(ticker, dono);
        return new AtivoTravado(paraDominio(documento), dono);
    }

    @Override
    public void salvarELiberar(AtivoTravado travado) {
        trava.salvarELiberar(paraDocumento(travado.ativo()), travado.dono());
    }

    @Override
    public void liberar(AtivoTravado travado) {
        trava.liberar(travado.ativo().ticker(), travado.dono());
    }

    @Override
    public void atualizarCotacoes(Map<String, BigDecimal> cotacoes) {
        for (Map.Entry<String, BigDecimal> cotacao : cotacoes.entrySet()) {
            Query porTicker = new Query(Criteria.where("_id").is(cotacao.getKey()));
            template.updateFirst(porTicker, new Update().set("cotacao", cotacao.getValue()),
                    AtivoNegociavelDocument.class);
        }
    }

    private static AtivoNegociavelDocument paraDocumento(AtivoNegociavel ativo) {
        return new AtivoNegociavelDocument(ativo.ticker(), ativo.nome(), ativo.bolsa(), ativo.moeda(), ativo.cambio(),
                ativo.cotacao(), ativo.quantidadeEmitida(), ativo.quantidadeDisponivel(),
                ativo.quantidadeReservada(), ativo.negociavel(), null);
    }

    private static AtivoNegociavel paraDominio(AtivoNegociavelDocument documento) {
        return new AtivoNegociavel(documento.ticker(), documento.nome(), documento.bolsa(), documento.moeda(),
                documento.cambio(), documento.cotacao(), documento.quantidadeEmitida(),
                documento.quantidadeDisponivel(), documento.quantidadeReservada(), documento.negociavel());
    }
}
