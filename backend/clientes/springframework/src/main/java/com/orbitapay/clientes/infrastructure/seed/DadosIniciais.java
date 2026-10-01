package com.orbitapay.clientes.infrastructure.seed;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.application.usecase.BloquearCliente;
import com.orbitapay.clientes.application.usecase.CadastrarCliente;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class DadosIniciais implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private final ClienteRepository repositorio;
    private final CadastrarCliente cadastrarCliente;
    private final BloquearCliente bloquearCliente;

    public DadosIniciais(ClienteRepository repositorio, CadastrarCliente cadastrarCliente,
            BloquearCliente bloquearCliente) {
        this.repositorio = repositorio;
        this.cadastrarCliente = cadastrarCliente;
        this.bloquearCliente = bloquearCliente;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!repositorio.vazio()) {
            return;
        }
        cadastrar("Ana Ribeiro", "ana@orbita.com", "123.456.789-09", "1234", "12480.55");
        cadastrar("Bruno Tavares", "bruno@orbita.com", "987.654.321-00", "4321", "3150.00");
        Cliente carla = cadastrar("Carla Mendes", "carla@orbita.com", "456.123.789-55", "1111", "890.20");
        bloquearCliente.executar(carla.id(), MotivoBloqueio.PIN);
        LOG.info("Clientes de demonstração cadastrados");
    }

    private Cliente cadastrar(String nome, String email, String cpf, String pin, String deposito) {
        return cadastrarCliente.executar(
                new CadastrarCliente.Comando(nome, email, cpf, pin, new BigDecimal(deposito)));
    }
}
