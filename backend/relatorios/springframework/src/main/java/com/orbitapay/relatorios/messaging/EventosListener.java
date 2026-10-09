package com.orbitapay.relatorios.messaging;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.orbitapay.relatorios.application.usecase.RegistrarFato;
import com.orbitapay.relatorios.application.usecase.RenomearCliente;
import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.messaging.mensagem.EventoDeClienteMensagem;
import com.orbitapay.relatorios.messaging.mensagem.OrdemMensagem;
import com.orbitapay.relatorios.messaging.mensagem.PagamentoMensagem;

@Component
public class EventosListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventosListener.class);

    private final RegistrarFato registrarFato;
    private final RenomearCliente renomearCliente;

    public EventosListener(RegistrarFato registrarFato, RenomearCliente renomearCliente) {
        this.registrarFato = registrarFato;
        this.renomearCliente = renomearCliente;
    }

    @RabbitListener(queues = "${mensageria.assinaturas.cliente-eventos.fila}", concurrency = "1")
    public void cliente(@Payload EventoDeClienteMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.clienteId());
        if ("CLIENTE_ATUALIZADO".equals(mensagem.evento())) {
            renomearCliente.executar(mensagem.clienteId(), mensagem.nome());
            return;
        }
        TipoDeFato tipo = switch (mensagem.evento()) {
            case "CLIENTE_CADASTRADO" -> TipoDeFato.CLIENTE_CADASTRADO;
            case "CLIENTE_SITUACAO_ALTERADA" ->
                mensagem.bloqueado() ? TipoDeFato.CLIENTE_BLOQUEADO : TipoDeFato.CLIENTE_DESBLOQUEADO;
            case "CLIENTE_REMOVIDO" -> TipoDeFato.CLIENTE_REMOVIDO;
            default -> null;
        };
        if (tipo != null) {
            registrarFato.executar(new Fato(mensagem.eventoId(), tipo, mensagem.clienteId(), null, 0,
                    mensagem.depositoInicial(), mensagem.motivo(), quando(mensagem.enviadoEm())), mensagem.nome());
        }
    }

    @RabbitListener(queues = "${mensageria.assinaturas.ordem-eventos.fila}")
    public void ordem(@Payload OrdemMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.ordemId());
        TipoDeFato tipo = switch (mensagem.evento()) {
            case "ORDEM_EXECUTADA" -> "VENDA".equals(mensagem.tipo()) ? TipoDeFato.VENDA_EXECUTADA : TipoDeFato.COMPRA_EXECUTADA;
            case "ORDEM_REJEITADA" -> TipoDeFato.ORDEM_REJEITADA;
            default -> null;
        };
        if (tipo != null) {
            registrarFato.executar(new Fato(mensagem.eventoId(), tipo, mensagem.clienteId(), mensagem.ticker(),
                    mensagem.quantidade(), mensagem.valorTotal(), tipo == TipoDeFato.ORDEM_REJEITADA ? mensagem.motivo()
                            : mensagem.tipo(), quando(mensagem.enviadoEm())), null);
        }
    }

    @RabbitListener(queues = "${mensageria.assinaturas.pagamento-eventos.fila}")
    public void pagamento(@Payload PagamentoMensagem mensagem) {
        LOG.info("RECEBIDO {} {}", mensagem.evento(), mensagem.pagamentoId());
        TipoDeFato tipo = switch (mensagem.evento()) {
            case "PAGAMENTO_CONFIRMADO" -> TipoDeFato.DEPOSITO_CONFIRMADO;
            case "PAGAMENTO_EXPIRADO" -> TipoDeFato.DEPOSITO_EXPIRADO;
            default -> null;
        };
        if (tipo != null) {
            registrarFato.executar(new Fato(mensagem.eventoId(), tipo, mensagem.clienteId(), null, 0,
                    mensagem.valorPago() == null ? mensagem.valor() : mensagem.valorPago(), mensagem.metodo(),
                    quando(mensagem.enviadoEm())), null);
        }
    }

    private static Instant quando(Instant enviadoEm) {
        return enviadoEm == null ? Instant.now() : enviadoEm;
    }
}
