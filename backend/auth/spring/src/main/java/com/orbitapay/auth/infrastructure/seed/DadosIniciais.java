package com.orbitapay.auth.infrastructure.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.entity.Perfil;
import com.orbitapay.auth.application.gateway.IFuncionarioGateway;
import com.orbitapay.auth.application.usecase.CredencialUseCase;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class DadosIniciais implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private final CredencialUseCase credencialUseCase;
    private final IFuncionarioGateway funcionarioGateway;

    public DadosIniciais(CredencialUseCase credencialUseCase, IFuncionarioGateway funcionarioGateway) {
        this.credencialUseCase = credencialUseCase;
        this.funcionarioGateway = funcionarioGateway;
    }

    @Override
    public void run(ApplicationArguments args) {
        cadastrarFuncionarioSeNaoExistir("Marina Gerente", "gerente@orbita.com", "0000", Perfil.GERENTE);
        cadastrarFuncionarioSeNaoExistir("Otávio Admin", "admin@orbita.com", "9999", Perfil.ADMIN);
        cadastrarCredencialDeDemonstracao("000000000000000000000001", "ana@orbita.com", "1234");
        cadastrarCredencialDeDemonstracao("000000000000000000000002", "bruno@orbita.com", "4321");
        cadastrarCredencialDeDemonstracao("000000000000000000000003", "carla@orbita.com", "1111");
    }

    private void cadastrarFuncionarioSeNaoExistir(String nome, String email, String pin, Perfil perfil) {
        if (!funcionarioGateway.existePorEmail(email)) {
            credencialUseCase.cadastrarFuncionarioDaEquipe(nome, email, pin, perfil);
            LOG.info("Conta da equipe criada: {} ({})", email, perfil);
        }
    }

    private void cadastrarCredencialDeDemonstracao(String clienteId, String email, String pin) {
        try {
            credencialUseCase.cadastrarCredencialDoCliente(clienteId, email, pin);
        } catch (RuntimeException erro) {
            LOG.warn("Credencial de demonstração {} não criada: {}", email, erro.getMessage());
        }
    }
}
