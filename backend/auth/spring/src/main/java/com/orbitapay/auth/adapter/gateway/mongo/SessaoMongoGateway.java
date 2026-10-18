package com.orbitapay.auth.adapter.gateway.mongo;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.auth.adapter.gateway.mongo.document.SessaoDocument;
import com.orbitapay.auth.adapter.gateway.mongo.repository.SessaoRepository;
import com.orbitapay.auth.adapter.interfaces.IGeradorDeHash;
import com.orbitapay.auth.entity.Perfil;
import com.orbitapay.auth.entity.Sessao;
import com.orbitapay.auth.application.gateway.ISessaoGateway;

@Component
public class SessaoMongoGateway implements ISessaoGateway {

    private final SessaoRepository sessaoRepository;
    private final IGeradorDeHash geradorDeHash;

    public SessaoMongoGateway(SessaoRepository sessaoRepository, IGeradorDeHash geradorDeHash) {
        this.sessaoRepository = sessaoRepository;
        this.geradorDeHash = geradorDeHash;
    }

    @Override
    public void salvar(Sessao sessao) {
        sessaoRepository.save(new SessaoDocument(geradorDeHash.gerarHashDoToken(sessao.token()),
                sessao.perfil().name(), sessao.sujeito(), sessao.clienteId(), sessao.nome(), sessao.expiraEm()));
    }

    @Override
    public Optional<Sessao> buscarPorToken(String token) {
        return sessaoRepository.findById(geradorDeHash.gerarHashDoToken(token))
                .map(documento -> new Sessao(token, Perfil.valueOf(documento.perfil()), documento.sujeito(),
                        documento.clienteId(), documento.nome(), documento.expiraEm()));
    }

    @Override
    public void removerPorToken(String token) {
        sessaoRepository.deleteById(geradorDeHash.gerarHashDoToken(token));
    }

    @Override
    public void removerTodasDoSujeito(String sujeito) {
        sessaoRepository.deleteBySujeito(sujeito);
    }
}
