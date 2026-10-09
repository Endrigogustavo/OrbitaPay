package com.orbitapay.contas.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.dto.OrdemRecebida;
import com.orbitapay.contas.application.usecase.CreditarVenda;
import com.orbitapay.contas.application.usecase.DebitarCompra;
import com.orbitapay.contas.messaging.mensagem.OrdemMensagem;

@Component
public class OrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrdemListener.class);

    private final DebitarCompra debitarCompra;
    private final CreditarVenda creditarVenda;

    public OrdemListener(DebitarCompra debitarCompra, CreditarVenda creditarVenda) {
        this.debitarCompra = debitarCompra;
        this.creditarVenda = creditarVenda;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-compra-solicitada.fila}")
    public void compraSolicitada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.compra-solicitada {}", mensagem);
        debitarCompra.executar(ordem(mensagem));
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-executada.fila}")
    public void ordemExecutada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.executada {}", mensagem);
        if (mensagem.venda()) {
            creditarVenda.executar(ordem(mensagem));
        }
    }

    private static OrdemRecebida ordem(OrdemMensagem mensagem) {
        return new OrdemRecebida(mensagem.ordemId(), mensagem.clienteId(), mensagem.ticker(), mensagem.quantidade(),
                mensagem.valorTotal());
    }
}
