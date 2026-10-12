package com.orbitapay.auth.infrastructure.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.usecase.RegistrarCredencial;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class ClientesIniciais implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ClientesIniciais.class);

    private static final List<RegistrarCredencial.Comando> CLIENTES = List.of(
            new RegistrarCredencial.Comando("000000000000000000000001", "ana@orbita.com", "1234"),
            new RegistrarCredencial.Comando("000000000000000000000002", "bruno@orbita.com", "4321"),
            new RegistrarCredencial.Comando("000000000000000000000003", "carla@orbita.com", "1111"));

    private final RegistrarCredencial registrarCredencial;

    public ClientesIniciais(RegistrarCredencial registrarCredencial) {
        this.registrarCredencial = registrarCredencial;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (RegistrarCredencial.Comando comando : CLIENTES) {
            try {
                registrarCredencial.executar(comando);
            } catch (RuntimeException erro) {
                LOG.warn("Credencial de demonstração {} não criada: {}. Se o e-mail já tem outra credencial, os "
                        + "bancos de Clientes e Auth estão de execuções diferentes: recrie os dois.",
                        comando.email(), erro.getMessage());
            }
        }
    }
}
