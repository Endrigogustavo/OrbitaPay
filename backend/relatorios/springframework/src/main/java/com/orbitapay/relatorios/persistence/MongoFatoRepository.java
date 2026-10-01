package com.orbitapay.relatorios.persistence;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.Periodo;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;

@Component
public class MongoFatoRepository implements FatoRepository {

    private final FatoDocumentRepository mongo;

    public MongoFatoRepository(FatoDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public boolean registrar(Fato fato) {
        try {
            mongo.insert(new FatoDocument(fato.eventoId(), fato.tipo().name(), fato.clienteId(), fato.ticker(),
                    fato.quantidade(), fato.valor(), fato.detalhe(), fato.ocorridoEm()));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    @Override
    public List<Fato> listarNoPeriodo(Periodo periodo) {
        return mongo.buscarNoPeriodo(periodo.inicio(), periodo.fim()).stream()
                .map(MongoFatoRepository::paraDominio).toList();
    }

    @Override
    public List<Fato> listarDoClienteNoPeriodo(String clienteId, Periodo periodo) {
        return mongo.buscarDoClienteNoPeriodo(clienteId, periodo.inicio(), periodo.fim()).stream()
                .map(MongoFatoRepository::paraDominio).toList();
    }

    private static Fato paraDominio(FatoDocument documento) {
        return new Fato(documento.eventoId(), TipoDeFato.valueOf(documento.tipo()), documento.clienteId(),
                documento.ticker(), documento.quantidade(), documento.valor(), documento.detalhe(),
                documento.ocorridoEm());
    }
}
