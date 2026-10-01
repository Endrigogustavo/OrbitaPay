package com.orbitapay.pagamentos.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.pagamentos.application.service.ProvedorIndisponivelException;
import com.orbitapay.pagamentos.domain.exception.PagamentoNaoEncontradoException;
import com.orbitapay.pagamentos.domain.exception.RegraDeNegocioException;
import com.orbitapay.pagamentos.web.dto.ErroResponse;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(PagamentoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(PagamentoNaoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, "PAGAMENTO_NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(ProvedorIndisponivelException.class)
    public ResponseEntity<ErroResponse> provedorIndisponivel(ProvedorIndisponivelException e) {
        return responder(HttpStatus.BAD_GATEWAY, "PROVEDOR_INDISPONIVEL",
                "Não foi possível gerar a cobrança agora. Tente outro método ou mais tarde");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> semSessao(MissingRequestHeaderException e) {
        return responder(HttpStatus.UNAUTHORIZED, "SESSAO_OBRIGATORIA", "Entre na sua conta");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Corpo da requisição inválido");
    }

    private static ResponseEntity<ErroResponse> responder(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(codigo, mensagem));
    }
}
