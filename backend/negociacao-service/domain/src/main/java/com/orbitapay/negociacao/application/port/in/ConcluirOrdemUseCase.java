package com.orbitapay.negociacao.application.port.in;

public interface ConcluirOrdemUseCase {

    void pagamentoAprovado(String ordemId);

    void pagamentoRecusado(String ordemId, String motivo);

    void acoesReservadas(String ordemId);

    void acoesIndisponiveis(String ordemId, String motivo);
}
