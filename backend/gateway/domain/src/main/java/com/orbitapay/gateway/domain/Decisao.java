package com.orbitapay.gateway.domain;

import java.util.Optional;

public sealed interface Decisao {

    record Liberado(Optional<String> clienteId, Optional<Perfil> perfil) implements Decisao {
    }

    record Recusado(int status, String codigo, String mensagem) implements Decisao {
    }

    static Decisao liberadoAnonimo() {
        return new Liberado(Optional.empty(), Optional.empty());
    }

    static Decisao liberado(Credencial sessao) {
        Optional<String> clienteId = sessao.perfil() == Perfil.CLIENTE ? Optional.of(sessao.sujeito()) : Optional.empty();
        return new Liberado(clienteId, Optional.of(sessao.perfil()));
    }

    static Decisao naoAutenticado() {
        return new Recusado(401, "SESSAO_OBRIGATORIA", "Entre na sua conta");
    }

    static Decisao semAssinatura() {
        return new Recusado(401, "ASSINATURA_OBRIGATORIA", "Confirme a operação com seu PIN");
    }

    static Decisao proibido() {
        return new Recusado(403, "ACESSO_NEGADO", "Acesso não permitido para este perfil");
    }
}
