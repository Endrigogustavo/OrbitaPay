package com.orbitapay.clientes.domain.event;

import java.time.Instant;

public sealed interface EventoDeCliente
        permits ClienteCadastrado, ClienteAtualizado, ClienteSituacaoAlterada, ClienteRemovido {

    String clienteId();

    Instant ocorridoEm();
}
