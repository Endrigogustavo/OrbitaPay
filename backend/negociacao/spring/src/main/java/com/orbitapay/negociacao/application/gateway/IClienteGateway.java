package com.orbitapay.negociacao.application.gateway;

public interface IClienteGateway {

    boolean clienteEstaBloqueado(String clienteId);
}
