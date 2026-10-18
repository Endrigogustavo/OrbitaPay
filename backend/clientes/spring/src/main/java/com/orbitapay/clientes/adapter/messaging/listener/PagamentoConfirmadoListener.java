package com.orbitapay.clientes.adapter.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.adapter.messaging.dto.PagamentoConfirmadoMessage;
import com.orbitapay.clientes.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.clientes.application.usecase.ContaUseCase;

@Component
public class PagamentoConfirmadoListener {

    private final ContaUseCase contaUseCase;

    public PagamentoConfirmadoListener(ContaUseCase contaUseCase) {
        this.contaUseCase = contaUseCase;
    }

    @RabbitListener(queues = CanaisDeMensageria.FILA_PAGAMENTOS_CONFIRMADOS)
    public void creditarDepositoNaConta(PagamentoConfirmadoMessage mensagem) {
        contaUseCase.creditarDepositoConfirmado(mensagem.clienteId(), mensagem.pagamentoId(), mensagem.valor(),
                mensagem.metodo());
    }
}
