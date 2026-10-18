package com.orbitapay.auth.application.gateway;

import java.util.Optional;

import com.orbitapay.auth.entity.Sessao;

public interface ISessaoGateway {

    void salvar(Sessao sessao);

    Optional<Sessao> buscarPorToken(String token);

    void removerPorToken(String token);

    void removerTodasDoSujeito(String sujeito);
}
