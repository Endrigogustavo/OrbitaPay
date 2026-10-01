package com.orbitapay.pagamentos.domain.exception;

public class PagamentoNaoEncontradoException extends RuntimeException {

    public PagamentoNaoEncontradoException(String pagamentoId) {
        super("Pagamento não encontrado: " + pagamentoId);
    }
}
