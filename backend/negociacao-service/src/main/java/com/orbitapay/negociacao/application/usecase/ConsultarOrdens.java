package com.orbitapay.negociacao.application.usecase;

import java.util.List;

import com.orbitapay.negociacao.application.port.in.ConsultarOrdensUseCase;
import com.orbitapay.negociacao.application.port.out.AtivoNegociavelRepository;
import com.orbitapay.negociacao.application.port.out.OrdemRepository;
import com.orbitapay.negociacao.domain.exception.RecursoNaoEncontradoException;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.model.Ordem;

public class ConsultarOrdens implements ConsultarOrdensUseCase {

    private final OrdemRepository ordens;
    private final AtivoNegociavelRepository ativos;

    public ConsultarOrdens(OrdemRepository ordens, AtivoNegociavelRepository ativos) {
        this.ordens = ordens;
        this.ativos = ativos;
    }

    @Override
    public Ordem buscarDoCliente(String ordemId, String clienteId) {
        return ordens.buscar(ordemId)
                .filter(ordem -> ordem.pertenceA(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem não encontrada: " + ordemId));
    }

    @Override
    public List<Ordem> listarDoCliente(String clienteId) {
        return ordens.listarDoCliente(clienteId);
    }

    @Override
    public AtivoNegociavel oferta(String ticker) {
        return ativos.buscar(ticker == null ? "" : ticker.toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ativo não negociado: " + ticker));
    }
}
