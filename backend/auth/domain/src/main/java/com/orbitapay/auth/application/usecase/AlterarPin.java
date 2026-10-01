package com.orbitapay.auth.application.usecase;

import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.domain.exception.CredencialNaoEncontradaException;
import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.domain.model.Credencial;
import com.orbitapay.auth.domain.model.Pin;
import com.orbitapay.auth.domain.repository.CredencialRepository;

public class AlterarPin {

    private final CredencialRepository repositorio;
    private final CodificadorDePin codificador;

    public AlterarPin(CredencialRepository repositorio, CodificadorDePin codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    public void executar(String clienteId, String pinAtual, String novoPin) {
        Credencial credencial = repositorio.buscarPorCliente(clienteId)
                .orElseThrow(() -> new CredencialNaoEncontradaException(clienteId));
        if (pinAtual == null || !pinAtual.matches("\\d{4}")
                || !codificador.confere(new Pin(pinAtual), credencial.pinCodificado())) {
            throw new RegraDeNegocioException("PIN atual incorreto");
        }
        Pin novo = new Pin(novoPin);
        if (codificador.confere(novo, credencial.pinCodificado())) {
            throw new RegraDeNegocioException("Escolha um PIN diferente do atual");
        }
        credencial.alterarPin(codificador.codificar(novo));
        repositorio.salvar(credencial);
    }
}
