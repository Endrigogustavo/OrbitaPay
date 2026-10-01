package com.orbitapay.contas.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.contas.application.exception.TravaIndisponivelException;
import com.orbitapay.contas.domain.exception.ContaBloqueadaException;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.exception.RegraDeNegocioException;
import com.orbitapay.contas.web.dto.ErroResponse;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(ContaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> naoEncontrada(ContaNaoEncontradaException e) {
        return responder(HttpStatus.NOT_FOUND, "CONTA_NAO_ENCONTRADA", e.getMessage());
    }

    @ExceptionHandler(ContaBloqueadaException.class)
    public ResponseEntity<ErroResponse> bloqueada(ContaBloqueadaException e) {
        return responder(HttpStatus.LOCKED, "CONTA_BLOQUEADA", e.getMessage());
    }

    @ExceptionHandler(TravaIndisponivelException.class)
    public ResponseEntity<ErroResponse> travada(TravaIndisponivelException e) {
        return responder(HttpStatus.CONFLICT, "RECURSO_OCUPADO", e.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException e) {
        return responder(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", e.getMessage());
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
