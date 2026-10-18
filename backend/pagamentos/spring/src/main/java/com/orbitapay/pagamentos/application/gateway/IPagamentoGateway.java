package com.orbitapay.pagamentos.application.gateway;

import java.util.List;
import java.util.Optional;

import com.orbitapay.pagamentos.entity.Pagamento;

public interface IPagamentoGateway {

    void salvar(Pagamento pagamento);

    Optional<Pagamento> buscarPorId(String pagamentoId);

    List<Pagamento> listarDoCliente(String clienteId);

    List<Pagamento> listarPendentes();

    void removerTodosDoCliente(String clienteId);
}
