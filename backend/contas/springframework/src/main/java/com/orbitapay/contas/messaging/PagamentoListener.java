package com.orbitapay.contas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.usecase.CreditarDeposito;
import com.orbitapay.contas.messaging.mensagem.PagamentoConfirmadoMensagem;

@Component
public class PagamentoListener {

    private static final Logger LOG = LoggerFactory.getLogger(PagamentoListener.class);

    private final CreditarDeposito creditarDeposito;

    public PagamentoListener(CreditarDeposito creditarDeposito) {
        this.creditarDeposito = creditarDeposito;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.pagamento-confirmado.fila}")
    public void pagamentoConfirmado(@Payload PagamentoConfirmadoMensagem mensagem) {
        LOG.info("RECEBIDO pagamento.confirmado {}", mensagem);
        creditarDeposito.executar(new CreditarDeposito.Comando(mensagem.pagamentoId(), mensagem.clienteId(),
                mensagem.valorRecebido(), mensagem.rotuloDoMetodo()));
    }
}
