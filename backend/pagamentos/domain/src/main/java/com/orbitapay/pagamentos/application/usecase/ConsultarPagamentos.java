package com.orbitapay.pagamentos.application.usecase;

import java.util.List;
import java.util.Optional;

import com.orbitapay.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.domain.repository.PagamentoRepository;

public class ConsultarPagamentos {

    private final PagamentoRepository repositorio;

    public ConsultarPagamentos(PagamentoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Pagamento doCliente(String pagamentoId, String clienteId) {
        Optional<Pagamento> pagamento = repositorio.buscar(pagamentoId);
        if (pagamento.isEmpty() || !pagamento.get().pertenceA(clienteId)) {
            throw new PagamentoNaoEncontradoException(pagamentoId);
        }
        return pagamento.get();
    }

    public List<Pagamento> listarDoCliente(String clienteId) {
        return repositorio.listarDoCliente(clienteId);
    }
}
