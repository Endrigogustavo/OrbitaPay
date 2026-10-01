package com.orbitapay.relatorios.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.orbitapay.relatorios.domain.exception.RegraDeNegocioException;
import com.orbitapay.relatorios.web.dto.ErroResponse;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> dataInvalida(MethodArgumentTypeMismatchException e) {
        return responder(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Use datas no formato AAAA-MM-DD");
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException e) {
        return responder(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> semSessao(MissingRequestHeaderException e) {
        return responder(HttpStatus.UNAUTHORIZED, "SESSAO_OBRIGATORIA", "Entre na sua conta");
    }

    private static ResponseEntity<ErroResponse> responder(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(codigo, mensagem));
    }
}
