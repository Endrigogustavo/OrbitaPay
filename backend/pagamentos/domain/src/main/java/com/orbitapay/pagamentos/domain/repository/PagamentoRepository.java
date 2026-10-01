package com.orbitapay.pagamentos.domain.repository;

import java.util.List;
import java.util.Optional;

import com.orbitapay.pagamentos.domain.model.Pagamento;

public interface PagamentoRepository {

    String proximoId();

    void salvar(Pagamento pagamento);

    Optional<Pagamento> buscar(String pagamentoId);

    List<Pagamento> listarDoCliente(String clienteId);

    List<Pagamento> listarPendentes(int limite);
}
