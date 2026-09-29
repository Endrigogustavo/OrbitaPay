package com.orbitapay.contas.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.adapter.in.messaging.mensagem.OrdemMensagem;
import com.orbitapay.contas.application.port.in.LiquidarOrdemUseCase;

@Component
public class OrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrdemListener.class);

    private final LiquidarOrdemUseCase liquidarOrdem;

    public OrdemListener(LiquidarOrdemUseCase liquidarOrdem) {
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

    private static LiquidarOrdemUseCase.Comando comando(OrdemMensagem mensagem) {
        return new LiquidarOrdemUseCase.Comando(mensagem.ordemId(), mensagem.clienteId(), mensagem.ticker(),
                mensagem.quantidade(), mensagem.valorTotal());
    }
}
