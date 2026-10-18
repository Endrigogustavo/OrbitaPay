package com.orbitapay.negociacao.application.gateway;

import java.util.function.Supplier;

public interface ITransacaoGateway {

    void executarEmTransacao(Runnable operacao);

    <T> T executarEmTransacaoERetornar(Supplier<T> operacao);
}
