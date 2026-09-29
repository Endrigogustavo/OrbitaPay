package com.orbitapay.clientes.application.usecase;

import com.orbitapay.clientes.application.dto.Credencial;
import com.orbitapay.clientes.application.port.in.AssinarOperacaoUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.EmissorDeCredencial;
import com.orbitapay.clientes.domain.exception.ClienteBloqueadoException;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Pin;

public class AssinarOperacao implements AssinarOperacaoUseCase {

    private final ClienteRepository repositorio;
    private final ConferenciaDePin conferencia;
    private final EmissorDeCredencial emissor;

    public AssinarOperacao(ClienteRepository repositorio, ConferenciaDePin conferencia, EmissorDeCredencial emissor) {
        this.repositorio = repositorio;
        this.conferencia = conferencia;
        this.emissor = emissor;
    }

    @Override
    public Credencial executar(String clienteId, String pin) {
        Cliente cliente = repositorio.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        if (cliente.bloqueado()) {
            throw new ClienteBloqueadoException();
        }
        conferencia.conferir(cliente, new Pin(pin));
        return emissor.emitirAssinatura(cliente.id());
    }
}
