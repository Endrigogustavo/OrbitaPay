package com.orbitapay.auth.infrastructure.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.usecase.CadastrarFuncionario;
import com.orbitapay.auth.domain.model.Email;
import com.orbitapay.auth.domain.model.Perfil;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class EquipeInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(EquipeInicial.class);

    private static final List<CadastrarFuncionario.Comando> EQUIPE = List.of(
            new CadastrarFuncionario.Comando("Marina Gerente", "gerente@orbita.com", "0000", Perfil.GERENTE),
            new CadastrarFuncionario.Comando("Otávio Admin", "admin@orbita.com", "9999", Perfil.ADMIN));

    private final FuncionarioRepository funcionarios;
    private final CadastrarFuncionario cadastrarFuncionario;

    public EquipeInicial(FuncionarioRepository funcionarios, CadastrarFuncionario cadastrarFuncionario) {
        this.funcionarios = funcionarios;
        this.cadastrarFuncionario = cadastrarFuncionario;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (CadastrarFuncionario.Comando comando : EQUIPE) {
            if (!funcionarios.existePorEmail(new Email(comando.email()))) {
                cadastrarFuncionario.executar(comando);
                LOG.info("Conta da equipe criada: {} ({})", comando.email(), comando.perfil());
            }
        }
    }
}
