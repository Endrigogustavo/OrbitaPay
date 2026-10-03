package com.orbitapay.auth.application.usecase;

import java.time.Instant;

import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.application.service.PublicadorDeEventosDeCredencial;
import com.orbitapay.auth.domain.event.CredencialBloqueadaPorPin;
import com.orbitapay.auth.domain.exception.PinIncorretoException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class ConferenciaDePin {

    private final CredencialRepository repositorio;
    private final CodificadorDePin codificador;
    private final PublicadorDeEventosDeCredencial publicador;

    public ConferenciaDePin(CredencialRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCredencial publicador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.publicador = publicador;
    }

    public void conferir(Credencial credencial, Pin pin) {
        if (codificador.confere(pin, credencial.pinCodificado())) {
            credencial.registrarPinCorreto();
            repositorio.salvar(credencial);
            return;
        }
        boolean bloqueouAgora = credencial.registrarPinIncorreto();
        repositorio.salvar(credencial);
        if (bloqueouAgora) {
            publicador.publicar(new CredencialBloqueadaPorPin(credencial.clienteId(), credencial.tentativasFalhas(),
                    Instant.now()));
        }
        throw new PinIncorretoException(credencial.tentativasRestantes(), bloqueouAgora);
    }
}
