package com.orbitapay.clientes.infrastructure.seed;

import java.math.BigDecimal;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.entity.Cliente;
import com.orbitapay.clientes.entity.MotivoBloqueio;
import com.orbitapay.clientes.application.gateway.IClienteGateway;
import com.orbitapay.clientes.application.usecase.ContaUseCase;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class DadosIniciais implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private final IClienteGateway clienteGateway;
    private final ContaUseCase contaUseCase;

    public DadosIniciais(IClienteGateway clienteGateway, ContaUseCase contaUseCase) {
        this.clienteGateway = clienteGateway;
        this.contaUseCase = contaUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        cadastrarClienteDeDemonstracao("000000000000000000000001", "Ana Ribeiro", "ana@orbita.com", "123.456.789-09",
                "12480.55", null);
        cadastrarClienteDeDemonstracao("000000000000000000000002", "Bruno Tavares", "bruno@orbita.com",
                "987.654.321-00", "3150.00", null);
        cadastrarClienteDeDemonstracao("000000000000000000000003", "Carla Mendes", "carla@orbita.com",
                "456.123.789-55", "890.20", MotivoBloqueio.PIN);
    }

    private void cadastrarClienteDeDemonstracao(String clienteId, String nome, String email, String cpf,
            String depositoInicial, MotivoBloqueio motivoBloqueio) {
        if (clienteGateway.buscarPorId(clienteId).isEmpty()) {
            clienteGateway.salvar(new Cliente(clienteId, nome, email, cpf, motivoBloqueio != null, motivoBloqueio,
                    Instant.now()));
            LOG.info("Cliente de demonstração cadastrado: {}", email);
        }
        contaUseCase.abrirContaDoCliente(clienteId, new BigDecimal(depositoInicial));
    }
}
