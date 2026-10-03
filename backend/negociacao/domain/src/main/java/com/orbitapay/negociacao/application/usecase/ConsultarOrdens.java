package com.orbitapay.negociacao.application.usecase;

import java.util.List;
import java.util.Optional;

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
        Optional<Ordem> ordem = ordens.buscar(ordemId);
        if (ordem.isEmpty() || !ordem.get().pertenceA(clienteId)) {
            throw new RecursoNaoEncontradoException("Ordem não encontrada: " + ordemId);
        }
        return ordem.get();
    }

    public List<Ordem> listarDoCliente(String clienteId) {
        return ordens.listarDoCliente(clienteId);
    }

    public AtivoNegociavel oferta(String ticker) {
        Optional<AtivoNegociavel> ativo = ativos.buscar(ticker == null ? "" : ticker.toUpperCase());
        if (ativo.isEmpty()) {
            throw new RecursoNaoEncontradoException("Ativo não negociado: " + ticker);
        }
        return ativo.get();
    }
}
