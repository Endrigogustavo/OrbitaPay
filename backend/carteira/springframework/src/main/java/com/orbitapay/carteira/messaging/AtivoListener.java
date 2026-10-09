package com.orbitapay.carteira.messaging;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.application.usecase.AtualizarCotacoes;
import com.orbitapay.carteira.application.usecase.RegistrarAtivoCotado;
import com.orbitapay.carteira.application.usecase.RemoverAtivoCotado;
import com.orbitapay.carteira.domain.model.AtivoCotado;
import com.orbitapay.carteira.messaging.mensagem.EventoDeAtivoMensagem;

@Component
public class AtivoListener {

    private static final Logger LOG = LoggerFactory.getLogger(AtivoListener.class);

    private final RegistrarAtivoCotado registrarAtivoCotado;
    private final RemoverAtivoCotado removerAtivoCotado;
    private final AtualizarCotacoes atualizarCotacoes;

    public AtivoListener(RegistrarAtivoCotado registrarAtivoCotado, RemoverAtivoCotado removerAtivoCotado,
            AtualizarCotacoes atualizarCotacoes) {
        this.registrarAtivoCotado = registrarAtivoCotado;
        this.removerAtivoCotado = removerAtivoCotado;
        this.atualizarCotacoes = atualizarCotacoes;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ativo-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeAtivoMensagem mensagem) {
        switch (mensagem.evento()) {
            case "ATIVO_LISTADO", "ATIVO_ATUALIZADO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                registrarAtivoCotado.executar(new AtivoCotado(mensagem.ticker(), mensagem.nome(), mensagem.moeda(),
                        mensagem.cambio(), mensagem.cotacao()));
            }
            case "ATIVO_REMOVIDO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                removerAtivoCotado.executar(mensagem.ticker());
            }
            case "COTACOES_ATUALIZADAS" -> atualizarCotacoes.executar(cotacoes(mensagem));
            default -> LOG.warn("Evento de ativo ignorado: {}", mensagem.evento());
        }
    }

    private static Map<String, BigDecimal> cotacoes(EventoDeAtivoMensagem mensagem) {
        Map<String, BigDecimal> cotacoes = new HashMap<>();
        if (mensagem.cotacoes() != null) {
            for (EventoDeAtivoMensagem.Cotacao cotacao : mensagem.cotacoes()) {
                cotacoes.put(cotacao.ticker(), cotacao.cotacao());
            }
        }
        return cotacoes;
    }
}
