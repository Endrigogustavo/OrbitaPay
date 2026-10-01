package com.orbitapay.ativos.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.service.PublicadorDeEventosDeAtivo;
import com.orbitapay.ativos.domain.event.AtivoAtualizado;
import com.orbitapay.ativos.domain.event.AtivoListado;
import com.orbitapay.ativos.domain.event.AtivoRemovido;
import com.orbitapay.ativos.domain.event.CotacoesAtualizadas;
import com.orbitapay.ativos.domain.event.DadosDoAtivo;
import com.orbitapay.ativos.domain.event.EventoDeAtivo;
import com.orbitapay.ativos.messaging.mensagem.AtivoMensagem;
import com.orbitapay.ativos.messaging.mensagem.AtivoRemovidoMensagem;
import com.orbitapay.ativos.messaging.mensagem.CotacoesAtualizadasMensagem;

@Component
public class RabbitPublicadorDeEventosDeAtivo implements PublicadorDeEventosDeAtivo {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDeAtivo.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitPublicadorDeEventosDeAtivo(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void publicar(EventoDeAtivo evento) {
        String eventoId = UUID.randomUUID().toString();
        switch (evento) {
            case AtivoListado e -> enviar("ativo-listado", mensagem(eventoId, "ATIVO_LISTADO", e.ativo(),
                    "Ativo " + e.ativo().ticker() + " listado na " + e.ativo().bolsa() + ".", e), true);
            case AtivoAtualizado e -> enviar("ativo-atualizado", mensagem(eventoId, "ATIVO_ATUALIZADO", e.ativo(),
                    "Ativo " + e.ativo().ticker() + " teve o cadastro atualizado.", e), true);
            case AtivoRemovido e -> enviar("ativo-removido", new AtivoRemovidoMensagem(eventoId, "ATIVO_REMOVIDO",
                    e.ticker(), "Ativo " + e.ticker() + " saiu da bolsa.", e.ocorridoEm()), true);
            case CotacoesAtualizadas e -> enviar("cotacoes-atualizadas", new CotacoesAtualizadasMensagem(eventoId,
                    "COTACOES_ATUALIZADAS", e.cotacoes().stream()
                            .map(c -> new CotacoesAtualizadasMensagem.Cotacao(c.ticker(), c.valor())).toList(),
                    e.ocorridoEm()), false);
        }
    }

    private static AtivoMensagem mensagem(String eventoId, String tipo, DadosDoAtivo dados, String texto,
            EventoDeAtivo evento) {
        return new AtivoMensagem(eventoId, tipo, dados.ticker(), dados.nome(), dados.setor(), dados.bolsa(),
                dados.moeda(), dados.cambio(), dados.cotacao(), dados.quantidadeEmitida(), texto, evento.ocorridoEm());
    }

    private void enviar(String nomeDoTopico, Object mensagem, boolean registrar) {
        String topico = mensageria.topico(nomeDoTopico);
        rabbitTemplate.convertAndSend(mensageria.exchange(), topico, mensagem);
        if (registrar) {
            LOG.info("PUBLICADO [{}] {}", topico, mensagem);
        } else {
            LOG.debug("PUBLICADO [{}] {}", topico, mensagem);
        }
    }
}
