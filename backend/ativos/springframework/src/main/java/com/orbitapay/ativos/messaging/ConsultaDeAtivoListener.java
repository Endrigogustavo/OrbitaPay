package com.orbitapay.ativos.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.application.usecase.ConsultarAtivos;
import com.orbitapay.ativos.domain.exception.RegraDeNegocioException;
import com.orbitapay.ativos.messaging.mensagem.AtivoConsultadoMensagem;
import com.orbitapay.ativos.messaging.mensagem.ConsultaDeAtivoMensagem;

@Component
public class ConsultaDeAtivoListener {

    private static final Logger LOG = LoggerFactory.getLogger(ConsultaDeAtivoListener.class);

    private final ConsultarAtivos consultarAtivos;

    public ConsultaDeAtivoListener(ConsultarAtivos consultarAtivos) {
        this.consultarAtivos = consultarAtivos;
    }

    @RabbitListener(queues = "${mensageria.fila-de-consultas}")
    public AtivoConsultadoMensagem responder(@Payload ConsultaDeAtivoMensagem consulta) {
        LOG.info("CONSULTA RECEBIDA de {} para o ativo {}", consulta.solicitante(), consulta.ticker());
        try {
            AtivoCotado ativo = consultarAtivos.procurar(consulta.ticker());
            if (ativo == null) {
                return AtivoConsultadoMensagem.naoEncontrado(consulta.ticker());
            }
            return AtivoConsultadoMensagem.de(ativo);
        } catch (RegraDeNegocioException e) {
            return AtivoConsultadoMensagem.naoEncontrado(consulta.ticker());
        }
    }
}
