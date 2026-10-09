package com.orbitapay.gateway.domain;

public record RegraDeAcesso(String metodo, String caminho, NivelDeAcesso nivel) {

    private static final String QUALQUER_METODO = "*";
    private static final String SUBCAMINHOS = "/**";

    public boolean atende(String metodoDaRequisicao, String caminhoDaRequisicao) {
        boolean metodoConfere = metodo.equals(QUALQUER_METODO) || metodo.equals(metodoDaRequisicao);
        if (!metodoConfere) {
            return false;
        }
        if (caminho.endsWith(SUBCAMINHOS)) {
            String base = caminho.substring(0, caminho.length() - SUBCAMINHOS.length());
            return caminhoDaRequisicao.startsWith(base + "/");
        }
        return caminho.equals(caminhoDaRequisicao);
    }
}
