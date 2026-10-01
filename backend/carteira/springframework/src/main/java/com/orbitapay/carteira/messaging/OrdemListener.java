package com.orbitapay.carteira.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.usecase.MovimentarCarteira;
import com.orbitapay.carteira.messaging.mensagem.OrdemMensagem;

@Component
public class OrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrdemListener.class);

    private final MovimentarCarteira movimentarCarteira;

    public OrdemListener(MovimentarCarteira movimentarCarteira) {
        this.movimentarCarteira = movimentarCarteira;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-venda-solicitada.fila}")
    public void vendaSolicitada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.venda-solicitada {}", mensagem);
        movimentarCarteira.reservarParaVenda(comando(mensagem));
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-executada.fila}")
    public void ordemExecutada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.executada {}", mensagem);
        if (mensagem.compra()) {
            movimentarCarteira.liquidarCompra(comando(mensagem));
        } else {
            movimentarCarteira.liquidarVenda(comando(mensagem));
        }
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-rejeitada.fila}")
    public void ordemRejeitada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.rejeitada {}", mensagem);
        if (!mensagem.compra()) {
            movimentarCarteira.cancelarVenda(comando(mensagem));
        }
    }

    private static MovimentarCarteira.Comando comando(OrdemMensagem mensagem) {
        return new MovimentarCarteira.Comando(mensagem.ordemId(), mensagem.clienteId(), mensagem.ticker(),
                mensagem.quantidade(), mensagem.precoUnitario());
    }
}
