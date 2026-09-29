package com.orbitapay.ativos.adapter.in.web;

public final class CabecalhosDoGateway {

    public static final String PERFIL = "X-Perfil";
    public static final String PERFIL_GERENTE = "GERENTE";

    private CabecalhosDoGateway() {
    }

    public static void exigirGerente(String perfil) {
        if (!PERFIL_GERENTE.equals(perfil)) {
            throw new AcessoNegadoException();
        }
    }
}
