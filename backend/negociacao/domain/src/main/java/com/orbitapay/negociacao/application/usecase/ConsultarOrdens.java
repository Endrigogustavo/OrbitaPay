package com.orbitapay.negociacao.application.usecase;

import java.util.List;

import com.orbitapay.negociacao.domain.exception.RecursoNaoEncontradoException;
import com.orbitapay.negociacao.domain.model.AtivoNegociavel;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.domain.repository.AtivoNegociavelRepository;
import com.orbitapay.negociacao.domain.repository.OrdemRepository;

public class ConsultarOrdens {

    private final OrdemRepository ordens;
    private final AtivoNegociavelRepository ativos;

    public ConsultarOrdens(OrdemRepository ordens, AtivoNegociavelRepository ativos) {
        this.ordens = ordens;
        this.ativos = ativos;
    }

    public Ordem buscarDoCliente(String ordemId, String clienteId) {
        return ordens.buscar(ordemId)
                .filter(ordem -> ordem.pertenceA(clienteId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem não encontrada: " + ordemId));
    }

    public List<Ordem> listarDoCliente(String clienteId) {
        return ordens.listarDoCliente(clienteId);
    }

    public AtivoNegociavel oferta(String ticker) {
        return ativos.buscar(ticker == null ? "" : ticker.toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ativo não negociado: " + ticker));
    }
}
