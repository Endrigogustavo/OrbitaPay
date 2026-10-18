package com.orbitapay.gateway.application.usecase;

import static com.orbitapay.gateway.entity.NivelDeAcesso.ADMIN;
import static com.orbitapay.gateway.entity.NivelDeAcesso.AUTENTICADO;
import static com.orbitapay.gateway.entity.NivelDeAcesso.CLIENTE;
import static com.orbitapay.gateway.entity.NivelDeAcesso.CLIENTE_COM_ASSINATURA;
import static com.orbitapay.gateway.entity.NivelDeAcesso.GERENTE;
import static com.orbitapay.gateway.entity.NivelDeAcesso.NEGADO;
import static com.orbitapay.gateway.entity.NivelDeAcesso.PUBLICO;
import static com.orbitapay.gateway.entity.NivelDeAcesso.SESSAO_OPCIONAL;

import java.util.List;

import com.orbitapay.gateway.application.dto.Decisao;
import com.orbitapay.gateway.application.dto.RegraDeAcesso;
import com.orbitapay.gateway.application.exception.AutenticacaoIndisponivelException;
import com.orbitapay.gateway.application.gateway.IAutenticacaoGateway;
import com.orbitapay.gateway.entity.Acesso;
import com.orbitapay.gateway.entity.NivelDeAcesso;
import com.orbitapay.gateway.service.PermissaoService;

public class AcessoUseCase {

    private static final String QUALQUER_METODO = "*";
    private static final String SUFIXO_DE_SUBCAMINHOS = "/**";

    private static final List<RegraDeAcesso> REGRAS_DE_ACESSO_POR_ROTA = List.of(
            new RegraDeAcesso("OPTIONS", "/**", PUBLICO),

            new RegraDeAcesso("POST", "/api/autenticacao/sessoes", PUBLICO),
            new RegraDeAcesso("GET", "/api/autenticacao/sessoes/atual", PUBLICO),
            new RegraDeAcesso("DELETE", "/api/autenticacao/sessoes/atual", PUBLICO),
            new RegraDeAcesso("POST", "/api/autenticacao/credenciais", SESSAO_OPCIONAL),
            new RegraDeAcesso("DELETE", "/api/autenticacao/credenciais/me", CLIENTE),
            new RegraDeAcesso("DELETE", "/api/autenticacao/credenciais/**", GERENTE),
            new RegraDeAcesso("POST", "/api/autenticacao/assinaturas", CLIENTE),
            new RegraDeAcesso("PUT", "/api/autenticacao/pin", CLIENTE),
            new RegraDeAcesso("GET", "/api/autenticacao/me", CLIENTE),
            new RegraDeAcesso("POST", "/api/autenticacao/me/gerente", CLIENTE_COM_ASSINATURA),

            new RegraDeAcesso("POST", "/api/clientes", AUTENTICADO),
            new RegraDeAcesso("POST", "/api/clientes/me/desbloqueio", CLIENTE_COM_ASSINATURA),
            new RegraDeAcesso("*", "/api/clientes/me", CLIENTE),
            new RegraDeAcesso("*", "/api/clientes/me/**", CLIENTE),
            new RegraDeAcesso("DELETE", "/api/clientes/**", ADMIN),
            new RegraDeAcesso("*", "/api/clientes", GERENTE),
            new RegraDeAcesso("*", "/api/clientes/**", GERENTE),

            new RegraDeAcesso("POST", "/api/contas/me/saques", CLIENTE_COM_ASSINATURA),
            new RegraDeAcesso("GET", "/api/contas/me", CLIENTE),
            new RegraDeAcesso("GET", "/api/contas", GERENTE),

            new RegraDeAcesso("GET", "/api/pagamentos", CLIENTE),
            new RegraDeAcesso("POST", "/api/pagamentos", CLIENTE),
            new RegraDeAcesso("GET", "/api/pagamentos/**", CLIENTE),

            new RegraDeAcesso("GET", "/api/ordens", CLIENTE),
            new RegraDeAcesso("POST", "/api/ordens", CLIENTE_COM_ASSINATURA),
            new RegraDeAcesso("GET", "/api/ordens/**", CLIENTE),

            new RegraDeAcesso("GET", "/api/carteiras/me", CLIENTE),
            new RegraDeAcesso("GET", "/api/carteiras/**", GERENTE),

            new RegraDeAcesso("GET", "/api/ativos", PUBLICO),
            new RegraDeAcesso("GET", "/api/ativos/**", PUBLICO),
            new RegraDeAcesso("*", "/api/ativos", ADMIN),
            new RegraDeAcesso("*", "/api/ativos/**", ADMIN),
            new RegraDeAcesso("GET", "/api/bolsas", PUBLICO));

    private final PermissaoService permissaoService;
    private final IAutenticacaoGateway autenticacaoGateway;

    public AcessoUseCase(PermissaoService permissaoService, IAutenticacaoGateway autenticacaoGateway) {
        this.permissaoService = permissaoService;
        this.autenticacaoGateway = autenticacaoGateway;
    }

    public Decisao decidirSeRequisicaoPodeSeguir(String metodoHttp, String caminho, String tokenDeSessao,
            String tokenDeAssinatura) {
        NivelDeAcesso nivelExigido = buscarNivelDeAcessoExigidoPelaRota(metodoHttp, caminho);
        if (nivelExigido == PUBLICO) {
            return liberarRequisicao(null);
        }
        if (nivelExigido == NEGADO) {
            return negarPorPerfilSemPermissao();
        }

        Acesso acessoDaSessao;
        try {
            acessoDaSessao = consultarAcessoDaSessaoNoAuth(tokenDeSessao, tokenDeAssinatura);
        } catch (AutenticacaoIndisponivelException e) {
            return nivelExigido == SESSAO_OPCIONAL ? liberarRequisicao(null) : negarPorAutenticacaoIndisponivel();
        }

        if (nivelExigido == SESSAO_OPCIONAL) {
            return liberarRequisicao(acessoDaSessao);
        }
        if (acessoDaSessao == null) {
            return negarPorFaltaDeSessao();
        }
        if (!permissaoService.perfilPodeAcessarONivel(acessoDaSessao.perfil(), nivelExigido)) {
            return negarPorPerfilSemPermissao();
        }
        if (permissaoService.nivelExigeAssinaturaDaOperacao(nivelExigido) && !acessoDaSessao.assinaturaValida()) {
            return negarPorFaltaDeAssinatura();
        }
        return liberarRequisicao(acessoDaSessao);
    }

    private Acesso consultarAcessoDaSessaoNoAuth(String tokenDeSessao, String tokenDeAssinatura) {
        if (tokenDeSessao == null || tokenDeSessao.isBlank()) {
            return null;
        }
        return autenticacaoGateway.consultarAcessoDaSessao(tokenDeSessao, tokenDeAssinatura).orElse(null);
    }

    private static NivelDeAcesso buscarNivelDeAcessoExigidoPelaRota(String metodoHttp, String caminho) {
        for (RegraDeAcesso regra : REGRAS_DE_ACESSO_POR_ROTA) {
            if (regraAtendeMetodoECaminho(regra, metodoHttp, caminho)) {
                return regra.nivel();
            }
        }
        return NEGADO;
    }

    private static boolean regraAtendeMetodoECaminho(RegraDeAcesso regra, String metodoHttp, String caminho) {
        boolean metodoConfere = regra.metodo().equals(QUALQUER_METODO) || regra.metodo().equals(metodoHttp);
        if (!metodoConfere) {
            return false;
        }
        if (regra.caminho().endsWith(SUFIXO_DE_SUBCAMINHOS)) {
            String caminhoBase = regra.caminho().substring(0, regra.caminho().length() - SUFIXO_DE_SUBCAMINHOS.length());
            return caminho.startsWith(caminhoBase + "/");
        }
        return regra.caminho().equals(caminho);
    }

    private static Decisao liberarRequisicao(Acesso acessoDaSessao) {
        return new Decisao(true, 200, null, null, acessoDaSessao);
    }

    private static Decisao negarPorFaltaDeSessao() {
        return new Decisao(false, 401, "SESSAO_OBRIGATORIA", "Entre na sua conta", null);
    }

    private static Decisao negarPorFaltaDeAssinatura() {
        return new Decisao(false, 401, "ASSINATURA_OBRIGATORIA", "Confirme a operação com seu PIN", null);
    }

    private static Decisao negarPorPerfilSemPermissao() {
        return new Decisao(false, 403, "ACESSO_NEGADO", "Acesso não permitido para este perfil", null);
    }

    private static Decisao negarPorAutenticacaoIndisponivel() {
        return new Decisao(false, 503, "AUTENTICACAO_INDISPONIVEL",
                "Não foi possível conferir sua sessão agora. Tente de novo em instantes", null);
    }
}
