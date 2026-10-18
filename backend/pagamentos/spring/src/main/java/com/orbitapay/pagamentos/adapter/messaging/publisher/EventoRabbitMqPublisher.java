package com.orbitapay.pagamentos.adapter.messaging.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.adapter.messaging.CanaisDeMensageria;
import com.orbitapay.pagamentos.adapter.messaging.dto.PagamentoConfirmadoMessage;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.application.gateway.IEventoGateway;

@Component
public class EventoRabbitMqPublisher implements IEventoGateway {

    private final RabbitTemplate rabbitTemplate;

    public EventoRabbitMqPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarPagamentoConfirmado(Pagamento pagamento) {
        PagamentoConfirmadoMessage mensagem = new PagamentoConfirmadoMessage(pagamento.id(), pagamento.clienteId(),
                pagamento.valorPago(), nomeDoMetodoParaOExtrato(pagamento.metodo()));
        rabbitTemplate.convertAndSend(CanaisDeMensageria.EXCHANGE_DE_EVENTOS,
                CanaisDeMensageria.EVENTO_PAGAMENTO_CONFIRMADO, mensagem);
    }

    private static String nomeDoMetodoParaOExtrato(MetodoDePagamento metodo) {
        return switch (metodo) {
            case PIX -> "Pix";
            case BOLETO -> "Boleto";
            case TED -> "TED";
        };
    }
}
