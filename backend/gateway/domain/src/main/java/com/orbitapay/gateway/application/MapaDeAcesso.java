package com.orbitapay.gateway.application;

import com.orbitapay.gateway.domain.NivelDeAcesso;

public final class MapaDeAcesso {

    private MapaDeAcesso() {
    }

    public static NivelDeAcesso nivel(String metodo, String caminho) {
        boolean leitura = "GET".equals(metodo);
        boolean criacao = "POST".equals(metodo);
        if ("OPTIONS".equals(metodo)) {
            return NivelDeAcesso.PUBLICO;
        }
        if (caminho.equals("/api/autenticacao/clientes") || caminho.equals("/api/autenticacao/gerente")) {
            return criacao ? NivelDeAcesso.PUBLICO : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/autenticacao/assinaturas")) {
            return criacao ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/autenticacao/pin")) {
            return "PUT".equals(metodo) ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/autenticacao/me")) {
            return leitura ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.startsWith("/api/autenticacao/")) {
            return NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/clientes") && criacao) {
            return NivelDeAcesso.PUBLICO_COM_SESSAO_OPCIONAL;
        }
        if (caminho.equals("/api/clientes/me/desbloqueio")) {
            return criacao ? NivelDeAcesso.CLIENTE_COM_ASSINATURA : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/clientes/me") || caminho.startsWith("/api/clientes/me/")) {
            return NivelDeAcesso.CLIENTE;
        }
        if (caminho.equals("/api/clientes") || caminho.startsWith("/api/clientes/")) {
            return NivelDeAcesso.GERENTE;
        }
        if (caminho.equals("/api/contas/me/saques")) {
            return criacao ? NivelDeAcesso.CLIENTE_COM_ASSINATURA : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/contas/me") || caminho.startsWith("/api/contas/me/")) {
            return NivelDeAcesso.CLIENTE;
        }
        if (caminho.equals("/api/contas")) {
            return leitura ? NivelDeAcesso.GERENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/pagamentos")) {
            return leitura || criacao ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.startsWith("/api/pagamentos/")) {
            return leitura ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/ordens")) {
            return leitura ? NivelDeAcesso.CLIENTE : criacao ? NivelDeAcesso.CLIENTE_COM_ASSINATURA : NivelDeAcesso.NEGADO;
        }
        if (caminho.startsWith("/api/ordens/")) {
            return leitura ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/carteiras/me")) {
            return leitura ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.startsWith("/api/carteiras/")) {
            return leitura ? NivelDeAcesso.GERENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/relatorios/me")) {
            return leitura ? NivelDeAcesso.CLIENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.startsWith("/api/relatorios/")) {
            return leitura ? NivelDeAcesso.GERENTE : NivelDeAcesso.NEGADO;
        }
        if (caminho.equals("/api/ativos") || caminho.startsWith("/api/ativos/")) {
            return leitura ? NivelDeAcesso.PUBLICO : NivelDeAcesso.GERENTE;
        }
        if (caminho.equals("/api/bolsas") || caminho.startsWith("/api/ofertas/")) {
            return leitura ? NivelDeAcesso.PUBLICO : NivelDeAcesso.NEGADO;
        }
        return NivelDeAcesso.NEGADO;
    }
}
