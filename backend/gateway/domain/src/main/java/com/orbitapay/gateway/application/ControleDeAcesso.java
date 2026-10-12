package com.orbitapay.gateway.application;

import java.time.Instant;

import com.orbitapay.gateway.domain.Credencial;
import com.orbitapay.gateway.domain.Decisao;
import com.orbitapay.gateway.domain.Perfil;
import com.orbitapay.gateway.domain.TipoDeCredencial;
import com.orbitapay.gateway.domain.VerificadorDeCredencial;

public class ControleDeAcesso {

    private final VerificadorDeCredencial verificador;

    public ControleDeAcesso(VerificadorDeCredencial verificador) {
        this.verificador = verificador;
    }

    public Decisao decidir(String metodo, String caminho, String tokenDeSessao, String tokenDeAssinatura) {
        Credencial sessao = credencialValida(tokenDeSessao, TipoDeCredencial.SESSAO);
        switch (MapaDeAcesso.nivel(metodo, caminho)) {
            case PUBLICO:
                return Decisao.liberadoAnonimo();
            case PUBLICO_COM_SESSAO_OPCIONAL:
                if (sessao != null && sessao.sessaoDaEquipe()) {
                    return Decisao.liberado(sessao);
                }
                return Decisao.liberadoAnonimo();
            case CLIENTE:
                return exigirSessao(sessao, Perfil.CLIENTE);
            case GERENTE:
                return exigirSessao(sessao, Perfil.GERENTE, Perfil.ADMIN);
            case ADMIN:
                return exigirSessao(sessao, Perfil.ADMIN);
            case CLIENTE_COM_ASSINATURA:
                return exigirAssinatura(sessao, tokenDeAssinatura);
            default:
                return Decisao.proibido();
        }
    }

    private Decisao exigirSessao(Credencial sessao, Perfil... perfisAceitos) {
        if (sessao == null) {
            return Decisao.naoAutenticado();
        }
        for (Perfil perfil : perfisAceitos) {
            if (sessao.sessaoDe(perfil)) {
                return Decisao.liberado(sessao);
            }
        }
        return Decisao.proibido();
    }

    private Decisao exigirAssinatura(Credencial sessao, String tokenDeAssinatura) {
        Decisao decisao = exigirSessao(sessao, Perfil.CLIENTE);
        if (!decisao.liberado()) {
            return decisao;
        }
        Credencial assinatura = credencialValida(tokenDeAssinatura, TipoDeCredencial.ASSINATURA);
        if (assinatura == null || !assinatura.assinaturaDe(sessao.sujeito())) {
            return Decisao.semAssinatura();
        }
        return decisao;
    }

    private Credencial credencialValida(String token, TipoDeCredencial tipoEsperado) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Credencial credencial = verificador.verificar(token);
        if (credencial == null || credencial.tipo() != tipoEsperado || !credencial.validaEm(Instant.now())) {
            return null;
        }
        return credencial;
    }
}
