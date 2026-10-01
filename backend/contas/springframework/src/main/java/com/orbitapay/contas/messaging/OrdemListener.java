package com.orbitapay.contas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.usecase.LiquidarOrdem;
import com.orbitapay.contas.messaging.mensagem.OrdemMensagem;

@Component
public class OrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrdemListener.class);

    private final LiquidarOrdem liquidarOrdem;

    public OrdemListener(LiquidarOrdem liquidarOrdem) {
        this.liquidarOrdem = liquidarOrdem;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-compra-solicitada.fila}")
    public void compraSolicitada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.compra-solicitada {}", mensagem);
        liquidarOrdem.debitarCompra(comando(mensagem));
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-executada.fila}")
    public void ordemExecutada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.executada {}", mensagem);
        if (mensagem.venda()) {
            liquidarOrdem.creditarVenda(comando(mensagem));
        }
    }

    private static LiquidarOrdem.Comando comando(OrdemMensagem mensagem) {
        return new LiquidarOrdem.Comando(mensagem.ordemId(), mensagem.clienteId(), mensagem.ticker(),
                mensagem.quantidade(), mensagem.valorTotal());
    }
}
