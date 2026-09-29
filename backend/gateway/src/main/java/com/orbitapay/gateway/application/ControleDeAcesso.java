package com.orbitapay.gateway.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import com.orbitapay.gateway.application.port.out.VerificadorDeCredencial;
import com.orbitapay.gateway.domain.Credencial;
import com.orbitapay.gateway.domain.Decisao;
import com.orbitapay.gateway.domain.Perfil;

public class ControleDeAcesso {

    private final VerificadorDeCredencial verificador;
    private final Clock relogio;

    public ControleDeAcesso(VerificadorDeCredencial verificador, Clock relogio) {
        this.verificador = verificador;
        this.relogio = relogio;
    }

    public Decisao decidir(String metodo, String caminho, String tokenDeSessao, String tokenDeAssinatura) {
        Optional<Credencial> sessao = credencialValida(tokenDeSessao);
        return switch (MapaDeAcesso.nivel(metodo, caminho)) {
            case PUBLICO -> Decisao.liberadoAnonimo();
            case PUBLICO_COM_SESSAO_OPCIONAL -> sessao.filter(c -> c.sessaoDe(Perfil.GERENTE))
                    .map(Decisao::liberado).orElseGet(Decisao::liberadoAnonimo);
            case CLIENTE -> exigirSessao(sessao, Perfil.CLIENTE);
            case GERENTE -> exigirSessao(sessao, Perfil.GERENTE);
            case CLIENTE_COM_ASSINATURA -> exigirAssinatura(sessao, tokenDeAssinatura);
            case NEGADO -> Decisao.proibido();
        };
    }

    private Decisao exigirSessao(Optional<Credencial> sessao, Perfil perfil) {
        if (sessao.isEmpty()) {
            return Decisao.naoAutenticado();
        }
        return sessao.get().sessaoDe(perfil) ? Decisao.liberado(sessao.get()) : Decisao.proibido();
    }

    private Decisao exigirAssinatura(Optional<Credencial> sessao, String tokenDeAssinatura) {
        Decisao decisao = exigirSessao(sessao, Perfil.CLIENTE);
        if (decisao instanceof Decisao.Recusado) {
            return decisao;
        }
        boolean assinado = credencialValida(tokenDeAssinatura)
                .filter(assinatura -> assinatura.assinaturaDe(sessao.get().sujeito()))
                .isPresent();
        return assinado ? decisao : Decisao.semAssinatura();
    }

    private Optional<Credencial> credencialValida(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Instant agora = Instant.now(relogio);
        return verificador.verificar(token).filter(c -> c.validaEm(agora));
    }
}
