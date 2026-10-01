package com.orbitapay.ativos.persistence;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.ativos.domain.model.Bolsa;
import com.orbitapay.ativos.domain.repository.BolsaRepository;

@Component
public class MongoBolsaRepository implements BolsaRepository {

    private final BolsaDocumentRepository mongo;

    public MongoBolsaRepository(BolsaDocumentRepository mongo) {
        this.mongo = mongo;
    }

    @Override
    public Optional<Bolsa> buscar(String codigo) {
        return codigo == null ? Optional.empty() : mongo.findById(codigo).map(MongoBolsaRepository::paraDominio);
    }

    @Override
    public List<Bolsa> listar() {
        return mongo.findAll().stream().map(MongoBolsaRepository::paraDominio).toList();
    }

    @Override
    public void salvar(Bolsa bolsa) {
        mongo.save(new BolsaDocument(bolsa.codigo(), bolsa.nome(), bolsa.cidade(), bolsa.moeda(), bolsa.cambio(),
                bolsa.fuso(), bolsa.abertura().toString(), bolsa.fechamento().toString()));
    }

    private static Bolsa paraDominio(BolsaDocument documento) {
        return new Bolsa(documento.codigo(), documento.nome(), documento.cidade(), documento.moeda(),
                documento.cambio(), documento.fuso(), LocalTime.parse(documento.abertura()),
                LocalTime.parse(documento.fechamento()));
    }
}
