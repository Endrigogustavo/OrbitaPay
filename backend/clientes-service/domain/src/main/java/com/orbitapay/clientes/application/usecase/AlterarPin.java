package com.orbitapay.clientes.application.usecase;

import com.orbitapay.clientes.application.port.in.AlterarPinUseCase;
import com.orbitapay.clientes.application.port.out.ClienteRepository;
import com.orbitapay.clientes.application.port.out.CodificadorDePin;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.Pin;

public class AlterarPin implements AlterarPinUseCase {

    private final ClienteRepository repositorio;
    private final CodificadorDePin codificador;

    public AlterarPin(ClienteRepository repositorio, CodificadorDePin codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }

    @Override
    public void executar(String clienteId, String pinAtual, String novoPin) {
        Cliente cliente = repositorio.buscarPorId(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
        if (pinAtual == null || !pinAtual.matches("\\d{4}")
                || !codificador.confere(new Pin(pinAtual), cliente.pinCodificado())) {
            throw new RegraDeNegocioException("PIN atual incorreto");
        }
        Pin novo = new Pin(novoPin);
        if (codificador.confere(novo, cliente.pinCodificado())) {
            throw new RegraDeNegocioException("Escolha um PIN diferente do atual");
        }
        cliente.alterarPin(codificador.codificar(novo));
        repositorio.salvar(cliente);
    }
}
