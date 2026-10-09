package com.orbitapay.negociacao.messaging;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.application.usecase.AtualizarCotacoes;
import com.orbitapay.negociacao.application.usecase.RegistrarAtivo;
import com.orbitapay.negociacao.application.usecase.RetirarAtivo;
import com.orbitapay.negociacao.messaging.mensagem.EventoDeAtivoMensagem;

@Component
public class AtivoListener {

    private static final Logger LOG = LoggerFactory.getLogger(AtivoListener.class);

    private final RegistrarAtivo registrarAtivo;
    private final RetirarAtivo retirarAtivo;
    private final AtualizarCotacoes atualizarCotacoes;

    public AtivoListener(RegistrarAtivo registrarAtivo, RetirarAtivo retirarAtivo,
            AtualizarCotacoes atualizarCotacoes) {
        this.registrarAtivo = registrarAtivo;
        this.retirarAtivo = retirarAtivo;
        this.atualizarCotacoes = atualizarCotacoes;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ativo-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeAtivoMensagem mensagem) {
        switch (mensagem.evento()) {
            case "ATIVO_LISTADO", "ATIVO_ATUALIZADO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                registrarAtivo.executar(new AtivoDoCatalogo(mensagem.ticker(), mensagem.nome(), mensagem.bolsa(),
                        mensagem.moeda(), mensagem.cambio(), mensagem.cotacao(), mensagem.quantidadeEmitida()));
            }
            case "ATIVO_REMOVIDO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                retirarAtivo.executar(mensagem.ticker());
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
