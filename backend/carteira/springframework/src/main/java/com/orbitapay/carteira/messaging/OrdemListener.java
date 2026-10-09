package com.orbitapay.carteira.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.dto.OrdemRecebida;
import com.orbitapay.carteira.application.usecase.CancelarReservaDeVenda;
import com.orbitapay.carteira.application.usecase.LiquidarCompra;
import com.orbitapay.carteira.application.usecase.LiquidarVenda;
import com.orbitapay.carteira.application.usecase.ReservarAcoesParaVenda;
import com.orbitapay.carteira.messaging.mensagem.OrdemMensagem;

@Component
public class OrdemListener {

    private static final Logger LOG = LoggerFactory.getLogger(OrdemListener.class);

    private final ReservarAcoesParaVenda reservarAcoesParaVenda;
    private final LiquidarCompra liquidarCompra;
    private final LiquidarVenda liquidarVenda;
    private final CancelarReservaDeVenda cancelarReservaDeVenda;

    public OrdemListener(ReservarAcoesParaVenda reservarAcoesParaVenda, LiquidarCompra liquidarCompra,
            LiquidarVenda liquidarVenda, CancelarReservaDeVenda cancelarReservaDeVenda) {
        this.reservarAcoesParaVenda = reservarAcoesParaVenda;
        this.liquidarCompra = liquidarCompra;
        this.liquidarVenda = liquidarVenda;
        this.cancelarReservaDeVenda = cancelarReservaDeVenda;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-venda-solicitada.fila}")
    public void vendaSolicitada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.venda-solicitada {}", mensagem);
        reservarAcoesParaVenda.executar(ordem(mensagem));
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-executada.fila}")
    public void ordemExecutada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.executada {}", mensagem);
        if (mensagem.compra()) {
            liquidarCompra.executar(ordem(mensagem));
        } else {
            liquidarVenda.executar(ordem(mensagem));
        }
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-rejeitada.fila}")
    public void ordemRejeitada(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO ordem.rejeitada {}", mensagem);
        if (!mensagem.compra()) {
            cancelarReservaDeVenda.executar(ordem(mensagem));
        }
    }

    private static OrdemRecebida ordem(OrdemMensagem mensagem) {
        return new OrdemRecebida(mensagem.ordemId(), mensagem.clienteId(), mensagem.ticker(), mensagem.quantidade(),
                mensagem.precoUnitario());
    }
}
