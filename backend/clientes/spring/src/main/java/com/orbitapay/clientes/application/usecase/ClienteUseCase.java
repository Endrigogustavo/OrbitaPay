package com.orbitapay.clientes.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.orbitapay.clientes.application.exception.NaoEncontradoException;
import com.orbitapay.clientes.application.gateway.IClienteGateway;
import com.orbitapay.clientes.application.gateway.ICredencialGateway;
import com.orbitapay.clientes.application.gateway.IEventoGateway;
import com.orbitapay.clientes.application.gateway.ITransacaoGateway;
import com.orbitapay.clientes.entity.Cliente;
import com.orbitapay.clientes.entity.MotivoBloqueio;
import com.orbitapay.clientes.exception.RegraDeNegocioException;
import com.orbitapay.clientes.service.ClienteService;

public class ClienteUseCase {

    private final ClienteService clienteService;
    private final IClienteGateway clienteGateway;
    private final ICredencialGateway credencialGateway;
    private final IEventoGateway eventoGateway;
    private final ITransacaoGateway transacaoGateway;
    private final ContaUseCase contaUseCase;

    public ClienteUseCase(ClienteService clienteService, IClienteGateway clienteGateway,
            ICredencialGateway credencialGateway, IEventoGateway eventoGateway, ITransacaoGateway transacaoGateway,
            ContaUseCase contaUseCase) {
        this.clienteService = clienteService;
        this.clienteGateway = clienteGateway;
        this.credencialGateway = credencialGateway;
        this.eventoGateway = eventoGateway;
        this.transacaoGateway = transacaoGateway;
        this.contaUseCase = contaUseCase;
    }

    public Cliente cadastrarClienteEAbrirConta(String clienteId, String nome, String email, String cpf,
            BigDecimal depositoInicial) {
        if (clienteId == null || clienteId.isBlank()) {
            throw new RegraDeNegocioException("Crie o acesso (e-mail e PIN) antes do cadastro");
        }
        Cliente novoCliente = clienteService.criarNovoCliente(clienteId, nome, email, cpf, Instant.now());
        if (clienteGateway.buscarPorId(clienteId).isPresent()) {
            throw new RegraDeNegocioException("Este acesso já tem cadastro");
        }
        if (clienteGateway.emailEmUsoPorOutroCliente(novoCliente.email(), clienteId)) {
            throw new RegraDeNegocioException("E-mail já cadastrado");
        }

        transacaoGateway.executarEmTransacao(() -> {
            clienteGateway.salvar(novoCliente);
            contaUseCase.abrirContaDoCliente(clienteId, depositoInicial);
        });
        return novoCliente;
    }

    public Cliente buscarCliente(String clienteId) {
        return clienteGateway.buscarPorId(clienteId)
                .orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado: " + clienteId));
    }

    public List<Cliente> listarClientes() {
        return clienteGateway.listarTodos();
    }

    public Cliente atualizarDadosDoCliente(String clienteId, String nome, String email, String cpf) {
        Cliente clienteAtual = buscarCliente(clienteId);
        Cliente clienteAtualizado = clienteService.atualizarDadosPessoais(clienteAtual, nome, email, cpf);
        boolean emailMudou = !clienteAtual.email().equals(clienteAtualizado.email());
        if (emailMudou) {
            if (clienteGateway.emailEmUsoPorOutroCliente(clienteAtualizado.email(), clienteId)) {
                throw new RegraDeNegocioException("E-mail já usado por outro cliente");
            }
            credencialGateway.atualizarEmailDeLogin(clienteId, clienteAtualizado.email());
        }
        clienteGateway.salvar(clienteAtualizado);
        return clienteAtualizado;
    }

    public Cliente bloquearCliente(String clienteId, MotivoBloqueio motivo) {
        return salvar(clienteService.bloquear(buscarCliente(clienteId), motivo));
    }

    public void bloquearClientePorErrosDePin(String clienteId) {
        clienteGateway.buscarPorId(clienteId)
                .filter(cliente -> !cliente.bloqueado())
                .ifPresent(cliente -> salvar(clienteService.bloquear(cliente, MotivoBloqueio.PIN)));
    }

    public Cliente desbloquearPeloProprioCliente(String clienteId) {
        return salvar(clienteService.desbloquearPeloProprioCliente(buscarCliente(clienteId)));
    }

    public Cliente desbloquearPeloGerente(String clienteId) {
        return salvar(clienteService.desbloquearPeloGerente(buscarCliente(clienteId)));
    }

    public boolean clienteEstaBloqueado(String clienteId) {
        return buscarCliente(clienteId).bloqueado();
    }

    public void removerClienteESuaConta(String clienteId) {
        buscarCliente(clienteId);
        transacaoGateway.executarEmTransacao(() -> {
            clienteGateway.remover(clienteId);
            contaUseCase.encerrarContaDoCliente(clienteId);
        });
        eventoGateway.publicarClienteRemovido(clienteId);
    }

    private Cliente salvar(Cliente cliente) {
        clienteGateway.salvar(cliente);
        return cliente;
    }
}
