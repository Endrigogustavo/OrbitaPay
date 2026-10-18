package com.orbitapay.pagamentos.application.gateway;

import com.orbitapay.pagamentos.entity.Pagamento;

public interface IEventoGateway {

    void publicarPagamentoConfirmado(Pagamento pagamento);
}
