package com.orbitapay.clientes.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import com.orbitapay.clientes.application.port.in.CadastrarClienteUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.CodificadorDePin;
import com.orbitapay.clientes.application.port.out.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteCadastrado;
import com.orbitapay.clientes.domain.exception.EmailJaCadastradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Cpf;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.NomeCompleto;
import com.orbitapay.clientes.domain.model.Pin;

public class CadastrarCliente implements CadastrarClienteUseCase {

    private final ClienteRepository repositorio;
    private final CodificadorDePin codificador;
    private final PublicadorDeEventosDeCliente publicador;
    private final Clock relogio;

    public CadastrarCliente(ClienteRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    @Override
    public Cliente executar(Comando comando) {
        NomeCompleto nome = new NomeCompleto(comando.nome());
        Email email = new Email(comando.email());
        if (repositorio.existeEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        Cpf cpf = new Cpf(comando.cpf());
        Pin pin = new Pin(comando.pin());
        BigDecimal depositoInicial = comando.depositoInicial() == null ? BigDecimal.ZERO : comando.depositoInicial();
        if (depositoInicial.signum() < 0) {
            throw new RegraDeNegocioException("Saldo inicial inválido");
        }
        Instant agora = Instant.now(relogio);
        Cliente cliente = Cliente.novo(repositorio.proximoId(), nome, email, cpf, codificador.codificar(pin), agora);
        repositorio.salvar(cliente);
        publicador.publicar(new ClienteCadastrado(cliente.id(), nome.valor(), email.valor(), depositoInicial, agora));
        return cliente;
    }
}
