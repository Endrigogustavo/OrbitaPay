package com.orbitapay.clientes.application.usecase;

import com.orbitapay.clientes.application.port.in.AutenticarClienteUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.EmissorDeCredencial;
import com.orbitapay.clientes.domain.exception.CredenciaisInvalidasException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Email;
import com.orbitapay.clientes.domain.model.Pin;

public class AutenticarCliente implements AutenticarClienteUseCase {

    private final ClienteRepository repositorio;
    private final ConferenciaDePin conferencia;
    private final EmissorDeCredencial emissor;

    public AutenticarCliente(ClienteRepository repositorio, ConferenciaDePin conferencia, EmissorDeCredencial emissor) {
        this.repositorio = repositorio;
        this.conferencia = conferencia;
        this.emissor = emissor;
    }

    @Override
    public Sessao executar(String email, String pin) {
        if (email == null || email.isBlank()) {
            throw new CredenciaisInvalidasException("Informe seu e-mail");
        }
        Cliente cliente = repositorio.buscarPorEmail(new Email(email))
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail não encontrado"));
        conferencia.conferir(cliente, new Pin(pin));
        return new Sessao(cliente, emissor.emitirSessaoDeCliente(cliente.id()));
    }
}
