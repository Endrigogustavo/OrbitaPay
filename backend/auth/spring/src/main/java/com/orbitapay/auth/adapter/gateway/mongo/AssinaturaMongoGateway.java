package com.orbitapay.auth.adapter.gateway.mongo;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.auth.adapter.gateway.mongo.document.AssinaturaDocument;
import com.orbitapay.auth.adapter.gateway.mongo.repository.AssinaturaRepository;
import com.orbitapay.auth.adapter.interfaces.IGeradorDeHash;
import com.orbitapay.auth.entity.Assinatura;
import com.orbitapay.auth.application.gateway.IAssinaturaGateway;

@Component
public class AssinaturaMongoGateway implements IAssinaturaGateway {

    private final AssinaturaRepository assinaturaRepository;
    private final IGeradorDeHash geradorDeHash;

    public AssinaturaMongoGateway(AssinaturaRepository assinaturaRepository, IGeradorDeHash geradorDeHash) {
        this.assinaturaRepository = assinaturaRepository;
        this.geradorDeHash = geradorDeHash;
    }

    @Override
    public void salvar(Assinatura assinatura) {
        assinaturaRepository.save(new AssinaturaDocument(geradorDeHash.gerarHashDoToken(assinatura.token()),
                assinatura.clienteId(), assinatura.expiraEm()));
    }

    @Override
    public Optional<Assinatura> buscarPorToken(String token) {
        return assinaturaRepository.findById(geradorDeHash.gerarHashDoToken(token))
                .map(documento -> new Assinatura(token, documento.clienteId(), documento.expiraEm()));
    }
}
