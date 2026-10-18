package com.orbitapay.ativos.adapter.gateway.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.ativos.entity.Bolsa;
import com.orbitapay.ativos.application.gateway.IBolsaGateway;
import com.orbitapay.ativos.adapter.gateway.mongo.document.BolsaDocument;
import com.orbitapay.ativos.adapter.gateway.mongo.repository.BolsaRepository;

@Component
public class BolsaMongoGateway implements IBolsaGateway {

    private final BolsaRepository bolsaRepository;

    public BolsaMongoGateway(BolsaRepository bolsaRepository) {
        this.bolsaRepository = bolsaRepository;
    }

    @Override
    public void salvar(Bolsa bolsa) {
        bolsaRepository.save(new BolsaDocument(bolsa.codigo(), bolsa.nome(), bolsa.cidade(), bolsa.moeda(),
                bolsa.cambio(), bolsa.fuso(), bolsa.abertura(), bolsa.fechamento()));
    }

    @Override
    public Optional<Bolsa> buscarPorCodigo(String codigo) {
        return bolsaRepository.findById(codigo).map(BolsaMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public List<Bolsa> listarTodas() {
        return bolsaRepository.findAll().stream().map(BolsaMongoGateway::converterDocumentoParaEntidade).toList();
    }

    private static Bolsa converterDocumentoParaEntidade(BolsaDocument documento) {
        return new Bolsa(documento.codigo(), documento.nome(), documento.cidade(), documento.moeda(),
                documento.cambio(), documento.fuso(), documento.abertura(), documento.fechamento());
    }
}
