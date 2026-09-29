package com.orbitapay.carteira.adapter.in.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.adapter.in.messaging.mensagem.OrdemMensagem;
import com.orbitapay.carteira.application.port.in.MovimentarCarteiraUseCase;

@Component
public class OrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrdemListener.class);

    private final MovimentarCarteiraUseCase movimentarCarteira;

    public OrdemListener(MovimentarCarteiraUseCase movimentarCarteira) {
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

    private static MovimentarCarteiraUseCase.Comando comando(OrdemMensagem mensagem) {
        return new MovimentarCarteiraUseCase.Comando(mensagem.ordemId(), mensagem.clienteId(), mensagem.ticker(),
                mensagem.quantidade(), mensagem.precoUnitario());
    }
}
