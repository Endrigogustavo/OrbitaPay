package com.orbitapay.pagamentos.application.usecase;

import java.util.List;

import com.orbitapay.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.domain.repository.PagamentoRepository;

public class ConsultarPagamentos {

    private final PagamentoRepository repositorio;

    public ConsultarPagamentos(PagamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Pagamento doCliente(String pagamentoId, String clienteId) {
        return repositorio.buscar(pagamentoId)
                .filter(pagamento -> pagamento.pertenceA(clienteId))
                .orElseThrow(() -> new PagamentoNaoEncontradoException(pagamentoId));
    }

    public List<Pagamento> listarDoCliente(String clienteId) {
        return repositorio.listarDoCliente(clienteId);
    }
}
