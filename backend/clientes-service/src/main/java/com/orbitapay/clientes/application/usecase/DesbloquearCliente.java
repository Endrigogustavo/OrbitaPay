package com.orbitapay.clientes.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.clientes.application.port.in.DesbloquearClienteUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.domain.model.Pin;

public class DesbloquearCliente implements DesbloquearClienteUseCase {

    private final ClienteRepository repositorio;
    private final ConferenciaDePin conferencia;
    private final PublicadorDeEventosDeCliente publicador;
    private final Clock relogio;

    public DesbloquearCliente(ClienteRepository repositorio, ConferenciaDePin conferencia,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.conferencia = conferencia;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public Cliente peloCliente(String clienteId, String pin) {
        Cliente cliente = buscar(clienteId);
        if (cliente.motivoBloqueio() != MotivoBloqueio.CLIENTE) {
            throw new RegraDeNegocioException("Somente o gerente pode desbloquear esta conta");
        }
        conferencia.conferir(cliente, new Pin(pin));
        cliente.desbloquearPeloCliente();
        return salvarEPublicar(cliente);
    }

    @Override
    public Cliente peloGerente(String clienteId) {
        Cliente cliente = buscar(clienteId);
        cliente.desbloquearPeloGerente();
        return salvarEPublicar(cliente);
    }

    private Cliente buscar(String clienteId) {
        return repositorio.buscarPorId(clienteId).orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
    }

    private Cliente salvarEPublicar(Cliente cliente) {
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), false, null, Instant.now(relogio)));
        return cliente;
    }
}
