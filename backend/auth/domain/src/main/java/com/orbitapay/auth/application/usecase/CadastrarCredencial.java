package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.dto.Sessao;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.domain.model.Perfil;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class CadastrarCredencial {

    private final CredencialRepository repositorio;
    private final RegistrarCredencial registrarCredencial;
    private final EmissorDeToken emissor;

    public CadastrarCredencial(CredencialRepository repositorio, RegistrarCredencial registrarCredencial,
            EmissorDeToken emissor) {
        this.repositorio = repositorio;
        this.registrarCredencial = registrarCredencial;
        this.emissor = emissor;
    }

    public Sessao executar(String email, String pin) {
        String clienteId = repositorio.proximoId();
        registrarCredencial.executar(new RegistrarCredencial.Comando(clienteId, email, pin));
        return new Sessao(Perfil.CLIENTE, clienteId, null, emissor.emitirSessaoDeCliente(clienteId));
    }
}
