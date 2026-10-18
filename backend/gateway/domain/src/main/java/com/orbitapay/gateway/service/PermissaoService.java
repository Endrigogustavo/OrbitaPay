package com.orbitapay.gateway.service;

import com.orbitapay.gateway.entity.NivelDeAcesso;
import com.orbitapay.gateway.entity.Perfil;

public class PermissaoService {

    public boolean perfilPodeAcessarONivel(Perfil perfil, NivelDeAcesso nivelExigido) {
        return switch (nivelExigido) {
            case PUBLICO, SESSAO_OPCIONAL, AUTENTICADO -> true;
            case CLIENTE, CLIENTE_COM_ASSINATURA -> perfil == Perfil.CLIENTE;
            case GERENTE -> perfil == Perfil.GERENTE || perfil == Perfil.ADMIN;
            case ADMIN -> perfil == Perfil.ADMIN;
            case NEGADO -> false;
        };
    }

    public boolean nivelExigeAssinaturaDaOperacao(NivelDeAcesso nivelExigido) {
        return nivelExigido == NivelDeAcesso.CLIENTE_COM_ASSINATURA;
    }
}
