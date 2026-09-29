package com.orbitapay.clientes.application.usecase;

import java.time.Clock;
import java.time.Instant;

import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.CodificadorDePin;
import com.orbitapay.clientes.application.port.out.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.exception.PinIncorretoException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Pin;

public class ConferenciaDePin {

    private final ClienteRepository repositorio;
    private final CodificadorDePin codificador;
    private final PublicadorDeEventosDeCliente publicador;
    private final Clock relogio;

    public ConferenciaDePin(ClienteRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCliente publicador, Clock relogio) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.publicador = publicador;
        this.relogio = relogio;
    }

    public void conferir(Cliente cliente, Pin pin) {
        if (codificador.confere(pin, cliente.pinCodificado())) {
            cliente.registrarPinCorreto();
            repositorio.salvar(cliente);
            return;
        }
        boolean bloqueouAgora = cliente.registrarPinIncorreto();
        repositorio.salvar(cliente);
        if (bloqueouAgora) {
            publicador.publicar(new ClienteSituacaoAlterada(cliente.id(), true, cliente.motivoBloqueio(),
                    Instant.now(relogio)));
        }
        throw new PinIncorretoException(cliente.tentativasRestantes(), bloqueouAgora);
    }
}
