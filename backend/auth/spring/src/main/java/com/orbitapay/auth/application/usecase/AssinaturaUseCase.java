package com.orbitapay.auth.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.orbitapay.auth.application.gateway.IAssinaturaGateway;
import com.orbitapay.auth.entity.Acesso;
import com.orbitapay.auth.entity.Assinatura;
import com.orbitapay.auth.entity.Sessao;

public class AssinaturaUseCase {

    private final CredencialUseCase credencialUseCase;
    private final SessaoUseCase sessaoUseCase;
    private final IAssinaturaGateway assinaturaGateway;
    private final Duration duracaoDaAssinatura;

    public AssinaturaUseCase(CredencialUseCase credencialUseCase, SessaoUseCase sessaoUseCase,
            IAssinaturaGateway assinaturaGateway, Duration duracaoDaAssinatura) {
        this.credencialUseCase = credencialUseCase;
        this.sessaoUseCase = sessaoUseCase;
        this.assinaturaGateway = assinaturaGateway;
        this.duracaoDaAssinatura = duracaoDaAssinatura;
    }

    public Assinatura assinarOperacaoComPin(String clienteId, String pin) {
        credencialUseCase.conferirPinEBloquearClienteAposTentativasErradas(
                credencialUseCase.buscarCredencialDoCliente(clienteId), pin);
        Assinatura assinatura = new Assinatura(UUID.randomUUID() + "-" + UUID.randomUUID(), clienteId,
                Instant.now().plus(duracaoDaAssinatura));
        assinaturaGateway.salvar(assinatura);
        return assinatura;
    }

    public Acesso consultarAcessoDaSessaoEDaAssinatura(String tokenDeSessao, String tokenDeAssinatura) {
        Sessao sessao = sessaoUseCase.buscarSessaoAtiva(tokenDeSessao);
        return new Acesso(sessao, assinaturaPertenceAoClienteEEstaValida(sessao, tokenDeAssinatura));
    }

    private boolean assinaturaPertenceAoClienteEEstaValida(Sessao sessao, String tokenDeAssinatura) {
        if (sessao.clienteId() == null || tokenDeAssinatura == null || tokenDeAssinatura.isBlank()) {
            return false;
        }
        return assinaturaGateway.buscarPorToken(tokenDeAssinatura)
                .map(assinatura -> assinatura.clienteId().equals(sessao.clienteId())
                        && assinatura.expiraEm().isAfter(Instant.now()))
                .orElse(false);
    }
}
