package com.orbitapay.clientes.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.application.service.RegistroDeCredencial;
import com.orbitapay.clientes.domain.event.ClienteCadastrado;
import com.orbitapay.clientes.domain.event.ClienteRemovido;
import com.orbitapay.clientes.domain.exception.EmailJaCadastradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Cpf;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.NomeCompleto;
import com.orbitapay.clientes.domain.repository.ClienteRepository;

public class CadastrarCliente {

    public record Comando(String nome, String email, String cpf, String pin, BigDecimal depositoInicial) {
    }

    private final ClienteRepository repositorio;
    private final RegistroDeCredencial registroDeCredencial;
    private final PublicadorDeEventosDeCliente publicador;

    public CadastrarCliente(ClienteRepository repositorio, RegistroDeCredencial registroDeCredencial,
            PublicadorDeEventosDeCliente publicador) {
        this.repositorio = repositorio;
        this.registroDeCredencial = registroDeCredencial;
        this.publicador = publicador;
    }

    public Cliente executar(Comando comando) {
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
        Cliente cliente = Cliente.novo(repositorio.proximoId(), nome, email, cpf, agora);
        repositorio.salvar(cliente);
        try {
            registroDeCredencial.registrar(cliente.id(), email.valor(), comando.pin());
        } catch (RegraDeNegocioException e) {
            repositorio.remover(cliente.id());
            throw e;
        } catch (RuntimeException e) {
            repositorio.remover(cliente.id());
            publicador.publicar(new ClienteRemovido(cliente.id(), Instant.now()));
            throw e;
        }
        publicador.publicar(new ClienteCadastrado(cliente.id(), nome.valor(), email.valor(), depositoInicial, agora));
        return cliente;
    }
}
