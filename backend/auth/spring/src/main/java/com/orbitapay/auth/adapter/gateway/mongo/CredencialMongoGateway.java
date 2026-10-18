package com.orbitapay.auth.adapter.gateway.mongo;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.auth.entity.Credencial;
import com.orbitapay.auth.application.gateway.ICredencialGateway;
import com.orbitapay.auth.adapter.gateway.mongo.document.CredencialDocument;
import com.orbitapay.auth.adapter.gateway.mongo.repository.CredencialRepository;

@Component
public class CredencialMongoGateway implements ICredencialGateway {

    private final CredencialRepository credencialRepository;

    public CredencialMongoGateway(CredencialRepository credencialRepository) {
        this.credencialRepository = credencialRepository;
    }

    @Override
    public void salvar(Credencial credencial) {
        credencialRepository.save(new CredencialDocument(credencial.clienteId(), credencial.email(),
                credencial.pinCodificado(), credencial.tentativasFalhas(), credencial.criadaEm()));
    }

    @Override
    public Optional<Credencial> buscarPorCliente(String clienteId) {
        return credencialRepository.findById(clienteId).map(CredencialMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public Optional<Credencial> buscarPorEmail(String email) {
        return credencialRepository.findByEmail(email).map(CredencialMongoGateway::converterDocumentoParaEntidade);
    }

    @Override
    public boolean emailEmUsoPorOutroCliente(String email, String clienteId) {
        return credencialRepository.existsByEmailAndClienteIdNot(email, clienteId);
    }

    @Override
    public void remover(String clienteId) {
        credencialRepository.deleteById(clienteId);
    }

    private static Credencial converterDocumentoParaEntidade(CredencialDocument documento) {
        return new Credencial(documento.clienteId(), documento.email(), documento.pinCodificado(),
                documento.tentativasFalhas(), documento.criadaEm());
    }
}
