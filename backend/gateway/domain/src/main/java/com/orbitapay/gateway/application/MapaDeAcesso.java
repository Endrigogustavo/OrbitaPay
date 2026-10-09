package com.orbitapay.gateway.application;

import static com.orbitapay.gateway.domain.NivelDeAcesso.CLIENTE;
import static com.orbitapay.gateway.domain.NivelDeAcesso.CLIENTE_COM_ASSINATURA;
import static com.orbitapay.gateway.domain.NivelDeAcesso.GERENTE;
import static com.orbitapay.gateway.domain.NivelDeAcesso.NEGADO;
import static com.orbitapay.gateway.domain.NivelDeAcesso.PUBLICO;
import static com.orbitapay.gateway.domain.NivelDeAcesso.PUBLICO_COM_SESSAO_OPCIONAL;

import java.util.List;

import com.orbitapay.gateway.domain.NivelDeAcesso;
import com.orbitapay.gateway.domain.RegraDeAcesso;

public final class MapaDeAcesso {

    private static final List<RegraDeAcesso> REGRAS = List.of(
            new RegraDeAcesso("OPTIONS", "/**", PUBLICO),

            new RegraDeAcesso("POST", "/api/autenticacao/clientes", PUBLICO),
            new RegraDeAcesso("POST", "/api/autenticacao/gerente", PUBLICO),
            new RegraDeAcesso("POST", "/api/autenticacao/assinaturas", CLIENTE),
            new RegraDeAcesso("PUT", "/api/autenticacao/pin", CLIENTE),
            new RegraDeAcesso("GET", "/api/autenticacao/me", CLIENTE),

            new RegraDeAcesso("POST", "/api/clientes", PUBLICO_COM_SESSAO_OPCIONAL),
            new RegraDeAcesso("POST", "/api/clientes/me/desbloqueio", CLIENTE_COM_ASSINATURA),
            new RegraDeAcesso("*", "/api/clientes/me/desbloqueio", NEGADO),
            new RegraDeAcesso("*", "/api/clientes/me", CLIENTE),
            new RegraDeAcesso("*", "/api/clientes/me/**", CLIENTE),
            new RegraDeAcesso("*", "/api/clientes", GERENTE),
            new RegraDeAcesso("*", "/api/clientes/**", GERENTE),

            new RegraDeAcesso("POST", "/api/contas/me/saques", CLIENTE_COM_ASSINATURA),
            new RegraDeAcesso("*", "/api/contas/me/saques", NEGADO),
            new RegraDeAcesso("*", "/api/contas/me", CLIENTE),
            new RegraDeAcesso("*", "/api/contas/me/**", CLIENTE),
            new RegraDeAcesso("GET", "/api/contas", GERENTE),

            new RegraDeAcesso("GET", "/api/pagamentos", CLIENTE),
            new RegraDeAcesso("POST", "/api/pagamentos", CLIENTE),
            new RegraDeAcesso("GET", "/api/pagamentos/**", CLIENTE),

            new RegraDeAcesso("GET", "/api/ordens", CLIENTE),
            new RegraDeAcesso("POST", "/api/ordens", CLIENTE_COM_ASSINATURA),
            new RegraDeAcesso("GET", "/api/ordens/**", CLIENTE),

            new RegraDeAcesso("GET", "/api/carteiras/me", CLIENTE),
            new RegraDeAcesso("GET", "/api/carteiras/**", GERENTE),

            new RegraDeAcesso("GET", "/api/relatorios/me", CLIENTE),
            new RegraDeAcesso("GET", "/api/relatorios/**", GERENTE),

            new RegraDeAcesso("GET", "/api/ativos", PUBLICO),
            new RegraDeAcesso("GET", "/api/ativos/**", PUBLICO),
            new RegraDeAcesso("*", "/api/ativos", GERENTE),
            new RegraDeAcesso("*", "/api/ativos/**", GERENTE),
            new RegraDeAcesso("GET", "/api/bolsas", PUBLICO),
            new RegraDeAcesso("GET", "/api/ofertas/**", PUBLICO));

    private MapaDeAcesso() {
    }

    public static NivelDeAcesso nivel(String metodo, String caminho) {
        for (RegraDeAcesso regra : REGRAS) {
            if (regra.atende(metodo, caminho)) {
                return regra.nivel();
            }
        }
        return NEGADO;
    }
}
