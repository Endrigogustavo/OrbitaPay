package com.orbitapay.negociacao.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.application.service.PublicadorDeEventosDeOrdem;
import com.orbitapay.negociacao.domain.event.EventoDeOrdem;
import com.orbitapay.negociacao.domain.event.OrdemDeCompraSolicitada;
import com.orbitapay.negociacao.domain.event.OrdemDeVendaSolicitada;
import com.orbitapay.negociacao.domain.event.OrdemExecutada;
import com.orbitapay.negociacao.domain.event.OrdemRejeitada;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.messaging.mensagem.OrdemMensagem;

@Component
public class RabbitPublicadorDeEventosDeOrdem implements PublicadorDeEventosDeOrdem {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDeOrdem.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitPublicadorDeEventosDeOrdem(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void publicar(EventoDeOrdem evento) {
        Ordem ordem = evento.ordem();
        String descricao = ordem.tipo() + " de " + ordem.quantidade() + " " + ordem.ticker();
        switch (evento) {
            case OrdemDeCompraSolicitada e -> enviar("compra-solicitada", e, "ORDEM_DE_COMPRA_SOLICITADA",
                    "Solicitado débito para " + descricao + ".");
            case OrdemDeVendaSolicitada e -> enviar("venda-solicitada", e, "ORDEM_DE_VENDA_SOLICITADA",
                    "Solicitada reserva de ações para " + descricao + ".");
            case OrdemExecutada e -> enviar("executada", e, "ORDEM_EXECUTADA", descricao + " executada.");
            case OrdemRejeitada e -> enviar("rejeitada", e, "ORDEM_REJEITADA",
                    descricao + " rejeitada: " + ordem.motivoRejeicao());
        }
    }

    private void enviar(String nomeDoTopico, EventoDeOrdem evento, String tipoDoEvento, String texto) {
        Ordem ordem = evento.ordem();
        OrdemMensagem mensagem = new OrdemMensagem(UUID.randomUUID().toString(), tipoDoEvento, ordem.id(),
                ordem.clienteId(), ordem.ticker(), ordem.tipo().name(), ordem.quantidade(), ordem.precoUnitario(),
                ordem.moeda(), ordem.valorTotal(), ordem.status().name(), ordem.motivoRejeicao(), texto,
                evento.ocorridoEm());
        String topico = mensageria.topico(nomeDoTopico);
        rabbitTemplate.convertAndSend(mensageria.exchange(), topico, mensagem);
        LOG.info("PUBLICADO [{}] {}", topico, mensagem);
    }
}
