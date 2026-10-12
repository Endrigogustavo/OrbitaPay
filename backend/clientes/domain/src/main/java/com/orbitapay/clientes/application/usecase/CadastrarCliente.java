package com.orbitapay.clientes.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteCadastrado;
import com.orbitapay.clientes.domain.exception.EmailJaCadastradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Cpf;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.NomeCompleto;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class CadastrarCliente {

    public record Comando(String clienteId, String nome, String email, String cpf, BigDecimal depositoInicial) {
    }

    private final ClienteRepository repositorio;
    private final PublicadorDeEventosDeCliente publicador;

    public CadastrarCliente(ClienteRepository repositorio, PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.publicador = publicador;
    }

    public Cliente executar(Comando comando) {
        if (comando.clienteId() == null || comando.clienteId().isBlank()) {
            throw new RegraDeNegocioException("Crie o acesso (e-mail e PIN) antes do cadastro");
        }
        if (repositorio.buscarPorId(comando.clienteId()).isPresent()) {
            throw new RegraDeNegocioException("Este acesso já tem cadastro");
        }
        NomeCompleto nome = new NomeCompleto(comando.nome());
        Email email = new Email(comando.email());
        if (repositorio.existeEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        Cpf cpf = new Cpf(comando.cpf());
        BigDecimal depositoInicial = comando.depositoInicial() == null ? BigDecimal.ZERO : comando.depositoInicial();
        if (depositoInicial.signum() < 0) {
            throw new RegraDeNegocioException("Saldo inicial inválido");
        }
        Instant agora = Instant.now();
        Cliente cliente = Cliente.novo(comando.clienteId(), nome, email, cpf, agora);
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteCadastrado(cliente.id(), nome.valor(), email.valor(), depositoInicial, agora));
        return cliente;
    }
}
