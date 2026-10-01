package com.orbitapay.pagamentos.messaging;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.orbitapay.pagamentos.application.service.PublicadorDeEventosDePagamento;
import com.orbitapay.pagamentos.domain.event.EventoDePagamento;
import com.orbitapay.pagamentos.domain.event.PagamentoConfirmado;
import com.orbitapay.pagamentos.domain.event.PagamentoExpirado;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.messaging.mensagem.PagamentoMensagem;

@Component
public class RabbitPublicadorDeEventosDePagamento implements PublicadorDeEventosDePagamento {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitPublicadorDeEventosDePagamento.class);

    private final RabbitTemplate rabbitTemplate;
    private final MensageriaProperties mensageria;

    public RabbitPublicadorDeEventosDePagamento(RabbitTemplate rabbitTemplate, MensageriaProperties mensageria) {
        this.rabbitTemplate = rabbitTemplate;
        this.mensageria = mensageria;
    }

    @Override
    public void publicar(EventoDePagamento evento) {
        Pagamento pagamento = evento.pagamento();
        String descricao = pagamento.metodo().rotulo() + " de " + pagamento.valor().formatado();
        switch (evento) {
            case PagamentoConfirmado e -> enviar("confirmado", e, "PAGAMENTO_CONFIRMADO",
                    descricao + " confirmado para o cliente " + pagamento.clienteId() + ".");
            case PagamentoExpirado e -> enviar("expirado", e, "PAGAMENTO_EXPIRADO",
                    descricao + " expirou sem pagamento.");
        }
    }

    private void enviar(String nomeDoTopico, EventoDePagamento evento, String tipoDoEvento, String texto) {
        Pagamento pagamento = evento.pagamento();
        PagamentoMensagem mensagem = new PagamentoMensagem(UUID.randomUUID().toString(), tipoDoEvento,
                pagamento.id(), pagamento.clienteId(), pagamento.metodo().name(), pagamento.valor().valor(),
                pagamento.valorPago() == null ? null : pagamento.valorPago().valor(), pagamento.provedor(),
                pagamento.concluidoEm(), texto, evento.ocorridoEm());
        String topico = mensageria.topico(nomeDoTopico);
        rabbitTemplate.convertAndSend(mensageria.exchange(), topico, mensagem);
        LOG.info("PUBLICADO [{}] {}", topico, mensagem);
    }
}
