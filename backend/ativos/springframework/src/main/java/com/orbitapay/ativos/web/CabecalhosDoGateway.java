package com.orbitapay.ativos.web;

public final class CabecalhosDoGateway {

    public static final String PERFIL = "X-Perfil";
    public static final String PERFIL_ADMIN = "ADMIN";

    private CabecalhosDoGateway() {
    }

    public static void exigirAdmin(String perfil) {
        if (!PERFIL_ADMIN.equals(perfil)) {
            throw new AcessoNegadoException();
        }
    }
}
