package com.orbitapay.clientes.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.application.service.PublicadorDeEventosDeCliente;
import com.orbitapay.clientes.domain.event.ClienteAtualizado;
import com.orbitapay.clientes.domain.event.ClienteCadastrado;
import com.orbitapay.clientes.domain.event.ClienteRemovido;
import com.orbitapay.clientes.domain.event.ClienteSituacaoAlterada;
import com.orbitapay.clientes.domain.event.EventoDeCliente;
import com.orbitapay.clientes.messaging.mensagem.ClienteAtualizadoMensagem;
import com.orbitapay.clientes.messaging.mensagem.ClienteCadastradoMensagem;
import com.orbitapay.clientes.messaging.mensagem.ClienteRemovidoMensagem;
import com.orbitapay.clientes.messaging.mensagem.ClienteSituacaoAlteradaMensagem;

@Component
public class RabbitPublicadorDeEventosDeCliente implements PublicadorDeEventosDeCliente {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDeCliente.class);

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String topicoCadastrado;
    private final String topicoAtualizado;
    private final String topicoSituacaoAlterada;
    private final String topicoRemovido;

    public RabbitPublicadorDeEventosDeCliente(RabbitTemplate rabbitTemplate,
            @Value("${mensageria.exchange}") String exchange,
            @Value("${mensageria.topicos.cliente-cadastrado}") String topicoCadastrado,
            @Value("${mensageria.topicos.cliente-atualizado}") String topicoAtualizado,
            @Value("${mensageria.topicos.cliente-situacao-alterada}") String topicoSituacaoAlterada,
            @Value("${mensageria.topicos.cliente-removido}") String topicoRemovido) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.topicoCadastrado = topicoCadastrado;
        this.topicoAtualizado = topicoAtualizado;
        this.topicoSituacaoAlterada = topicoSituacaoAlterada;
        this.topicoRemovido = topicoRemovido;
    }

    @Override
    public void publicar(EventoDeCliente evento) {
        String eventoId = UUID.randomUUID().toString();
        switch (evento) {
            case ClienteCadastrado e -> enviar(topicoCadastrado, new ClienteCadastradoMensagem(eventoId,
                    "CLIENTE_CADASTRADO", e.clienteId(), e.nome(), e.email(), e.depositoInicial(),
                    "Cliente " + e.clienteId() + " (" + e.nome() + ") foi cadastrado.", e.ocorridoEm()));
            case ClienteAtualizado e -> enviar(topicoAtualizado, new ClienteAtualizadoMensagem(eventoId,
                    "CLIENTE_ATUALIZADO", e.clienteId(), e.nome(), e.email(),
                    "Cliente " + e.clienteId() + " teve os dados atualizados.", e.ocorridoEm()));
            case ClienteSituacaoAlterada e -> enviar(topicoSituacaoAlterada, new ClienteSituacaoAlteradaMensagem(
                    eventoId, "CLIENTE_SITUACAO_ALTERADA", e.clienteId(), e.bloqueado(),
                    e.motivo() == null ? null : e.motivo().name(),
                    "Cliente " + e.clienteId() + (e.bloqueado() ? " foi bloqueado." : " foi desbloqueado."),
                    e.ocorridoEm()));
            case ClienteRemovido e -> enviar(topicoRemovido, new ClienteRemovidoMensagem(eventoId,
                    "CLIENTE_REMOVIDO", e.clienteId(), "Cliente " + e.clienteId() + " foi removido.",
                    e.ocorridoEm()));
        }
    }

    private void enviar(String topico, Object mensagem) {
        rabbitTemplate.convertAndSend(exchange, topico, mensagem);
        LOG.info("PUBLICADO [{}] {}", topico, mensagem);
    }
}
