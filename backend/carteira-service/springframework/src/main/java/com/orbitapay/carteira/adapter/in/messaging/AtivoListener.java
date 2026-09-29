package com.orbitapay.carteira.adapter.in.messaging;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.adapter.in.messaging.mensagem.EventoDeAtivoMensagem;
import com.orbitapay.carteira.application.port.in.SincronizarCotacoesUseCase;
import com.orbitapay.carteira.domain.model.AtivoCotado;

@Component
public class AtivoListener {

    private static final Logger LOG = LoggerFactory.getLogger(AtivoListener.class);

    private final SincronizarCotacoesUseCase sincronizarCotacoes;

    public AtivoListener(SincronizarCotacoesUseCase sincronizarCotacoes) {
        this.sincronizarCotacoes = sincronizarCotacoes;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ativo-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeAtivoMensagem mensagem) {
        switch (mensagem.evento()) {
            case "ATIVO_LISTADO", "ATIVO_ATUALIZADO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                sincronizarCotacoes.registrar(new AtivoCotado(mensagem.ticker(), mensagem.nome(), mensagem.moeda(),
                        mensagem.cambio(), mensagem.cotacao()));
            }
            case "ATIVO_REMOVIDO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                sincronizarCotacoes.remover(mensagem.ticker());
            }
            case "COTACOES_ATUALIZADAS" -> sincronizarCotacoes.atualizarCotacoes(cotacoes(mensagem));
            default -> LOG.warn("Evento de ativo ignorado: {}", mensagem.evento());
        }
    }

    private static Map<String, BigDecimal> cotacoes(EventoDeAtivoMensagem mensagem) {
        return mensagem.cotacoes() == null ? Map.of()
                : mensagem.cotacoes().stream().collect(Collectors.toMap(EventoDeAtivoMensagem.Cotacao::ticker,
                        EventoDeAtivoMensagem.Cotacao::cotacao, (a, b) -> b));
    }
}
