package com.orbitapay.clientes.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.orbitapay.clientes.application.service.AutenticacaoIndisponivelException;
import com.orbitapay.clientes.application.service.RegistroDeCredencial;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.messaging.mensagem.RegistroDeCredencialMensagem;
import com.orbitapay.clientes.messaging.mensagem.RegistroDeCredencialRespostaMensagem;

/** Request/reply na fila ponto a ponto da Autenticação (direct reply-to), no mesmo molde de ativos.consultas. */
@Component
public class RabbitRegistroDeCredencial implements RegistroDeCredencial {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitRegistroDeCredencial.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitRegistroDeCredencial(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void registrar(String clienteId, String email, String pin) {
        LOG.info("SOLICITANDO credencial ao contexto de Autenticação para o cliente {}", clienteId);
        RegistroDeCredencialRespostaMensagem resposta;
        try {
            resposta = rabbitTemplate.convertSendAndReceiveAsType("", mensageria.filaDeRegistrosDeCredencial(),
                    new RegistroDeCredencialMensagem(clienteId, email, pin),
                    new ParameterizedTypeReference<RegistroDeCredencialRespostaMensagem>() {
                    });
        } catch (AmqpException e) {
            LOG.warn("Contexto de Autenticação indisponível para o cliente {}: {}", clienteId, e.getMessage());
            throw new AutenticacaoIndisponivelException();
        }
        if (resposta == null) {
            LOG.warn("Contexto de Autenticação não respondeu a tempo para o cliente {}", clienteId);
            throw new AutenticacaoIndisponivelException();
        }
        if (!resposta.registrada()) {
            throw new RegraDeNegocioException(resposta.motivo());
        }
    }
}
