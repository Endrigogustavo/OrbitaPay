package com.orbitapay.pagamentos.adapter.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.pagamentos.adapter.messaging.dto.ClienteRemovidoMessage;
import com.orbitapay.pagamentos.application.usecase.PagamentoUseCase;

@Component
public class ClienteRemovidoListener {

    private final PagamentoUseCase pagamentoUseCase;

    public ClienteRemovidoListener(PagamentoUseCase pagamentoUseCase) {
        this.pagamentoUseCase = pagamentoUseCase;
    }

    @RabbitListener(queues = CanaisDeMensageria.FILA_CLIENTES_REMOVIDOS)
    public void removerPagamentosDoClienteRemovido(ClienteRemovidoMessage mensagem) {
        pagamentoUseCase.removerPagamentosDoCliente(mensagem.clienteId());
    }
}
