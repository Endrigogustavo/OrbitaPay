package com.orbitapay.gateway.domain;

public record Decisao(boolean liberado, int status, String codigo, String mensagem, String clienteId, Perfil perfil) {

    public static Decisao liberadoAnonimo() {
        return new Decisao(true, 200, null, null, null, null);
    }

    public static Decisao liberado(Credencial sessao) {
        String clienteId = sessao.perfil() == Perfil.CLIENTE ? sessao.sujeito() : null;
        return new Decisao(true, 200, null, null, clienteId, sessao.perfil());
    }

    public static Decisao naoAutenticado() {
        return new Decisao(false, 401, "SESSAO_OBRIGATORIA", "Entre na sua conta", null, null);
    }

    public static Decisao semAssinatura() {
        return new Decisao(false, 401, "ASSINATURA_OBRIGATORIA", "Confirme a operação com seu PIN", null, null);
    }

    public static Decisao proibido() {
        return new Decisao(false, 403, "ACESSO_NEGADO", "Acesso não permitido para este perfil", null, null);
    }
}
