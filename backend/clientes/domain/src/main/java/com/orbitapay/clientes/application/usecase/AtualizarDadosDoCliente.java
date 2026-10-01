package com.orbitapay.clientes.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteAtualizado;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Cpf;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.NomeCompleto;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class AtualizarDadosDoCliente {

    public record Comando(String clienteId, String nome, String email, String cpf) {
    }

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;
    private final Clock relogio;

    public AtualizarDadosDoCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador,
            Clock relogio) {
        this.repositorio = repositorio;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public Cliente executar(Comando comando) {
        Cliente cliente = repositorio.buscarPorId(comando.clienteId())
                .orElseThrow(() -> new ClienteNaoEncontradoException(comando.clienteId()));
        NomeCompleto nome = new NomeCompleto(comando.nome());
        Email email = new Email(comando.email());
        if (repositorio.existeEmailDeOutroCliente(email, cliente.id())) {
            throw new RegraDeNegocioException("E-mail já usado por outro cliente");
        }
        cliente.atualizarDados(nome, email);
        if (comando.cpf() != null) {
            cliente.corrigirCpf(new Cpf(comando.cpf()));
        }
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteAtualizado(cliente.id(), nome.valor(), email.valor(), Instant.now(relogio)));
        return cliente;
    }
}
