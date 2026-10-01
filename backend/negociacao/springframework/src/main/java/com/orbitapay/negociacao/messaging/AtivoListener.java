package com.orbitapay.negociacao.messaging;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;
import com.orbitapay.negociacao.application.usecase.SincronizarAtivos;
import com.orbitapay.negociacao.messaging.mensagem.EventoDeAtivoMensagem;

@Component
public class AtivoListener {

    private static final Logger LOG = LoggerFactory.getLogger(AtivoListener.class);

    private final SincronizarAtivos sincronizarAtivos;

    public AtivoListener(SincronizarAtivos sincronizarAtivos) {
        this.sincronizarAtivos = sincronizarAtivos;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ativo-eventos.fila}", concurrency = "1")
    public void receber(@Payload EventoDeAtivoMensagem mensagem) {
        switch (mensagem.evento()) {
            case "ATIVO_LISTADO", "ATIVO_ATUALIZADO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                sincronizarAtivos.registrar(new AtivoDoCatalogo(mensagem.ticker(), mensagem.nome(), mensagem.bolsa(),
                        mensagem.moeda(), mensagem.cambio(), mensagem.cotacao(), mensagem.quantidadeEmitida()));
            }
            case "ATIVO_REMOVIDO" -> {
                LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ticker());
                sincronizarAtivos.retirar(mensagem.ticker());
            }
            case "COTACOES_ATUALIZADAS" -> sincronizarAtivos.atualizarCotacoes(cotacoes(mensagem));
            default -> LOG.warn("Evento de ativo ignorado: {}", mensagem.evento());
        }
    }

    private static Map<String, BigDecimal> cotacoes(EventoDeAtivoMensagem mensagem) {
        return mensagem.cotacoes() == null ? Map.of()
                : mensagem.cotacoes().stream().collect(Collectors.toMap(EventoDeAtivoMensagem.Cotacao::ticker,
                        EventoDeAtivoMensagem.Cotacao::cotacao, (a, b) -> b));
    }
}
