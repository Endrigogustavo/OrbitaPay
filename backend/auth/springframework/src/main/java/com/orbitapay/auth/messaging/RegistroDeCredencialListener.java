package com.orbitapay.auth.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.application.usecase.RegistrarCredencial;
import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.messaging.mensagem.RegistroDeCredencialMensagem;
import com.orbitapay.auth.messaging.mensagem.RegistroDeCredencialRespostaMensagem;

@Component
public class RegistroDeCredencialListener {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroDeCredencialListener.class);

    private final RegistrarCredencial registrarCredencial;

    public RegistroDeCredencialListener(RegistrarCredencial registrarCredencial) {
        this.registrarCredencial = registrarCredencial;
    }

    @RabbitListener(queues = "${mensageria.fila-de-registros}")
    public RegistroDeCredencialRespostaMensagem registrar(@Payload RegistroDeCredencialMensagem pedido) {
        LOG.info("REGISTRO DE CREDENCIAL recebido para o cliente {}", pedido.clienteId());
        try {
            registrarCredencial.executar(new RegistrarCredencial.Comando(pedido.clienteId(), pedido.email(),
                    pedido.pin()));
            return RegistroDeCredencialRespostaMensagem.sucesso();
        } catch (RegraDeNegocioException e) {
            LOG.info("REGISTRO DE CREDENCIAL recusado para o cliente {}: {}", pedido.clienteId(), e.getMessage());
            return RegistroDeCredencialRespostaMensagem.recusada(e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("Falha ao registrar a credencial do cliente {}", pedido.clienteId(), e);
            return RegistroDeCredencialRespostaMensagem.recusada("Não foi possível registrar o PIN agora");
        }
    }
}
