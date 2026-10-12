package com.orbitapay.clientes.web;

public final class CabecalhosDoGateway {

    public static final String CLIENTE_ID = "X-Cliente-Id";
    public static final String PERFIL = "X-Perfil";
    public static final String PERFIL_GERENTE = "GERENTE";
    public static final String PERFIL_ADMIN = "ADMIN";

    private CabecalhosDoGateway() {
    }

    public static boolean daEquipe(String perfil) {
        return PERFIL_GERENTE.equals(perfil) || PERFIL_ADMIN.equals(perfil);
    }

    public static void exigirGerente(String perfil) {
        if (!daEquipe(perfil)) {
            throw new AcessoNegadoException();
        }
    }

    public static void exigirAdmin(String perfil) {
        if (!PERFIL_ADMIN.equals(perfil)) {
            throw new AcessoNegadoException();
        }
    }
}
