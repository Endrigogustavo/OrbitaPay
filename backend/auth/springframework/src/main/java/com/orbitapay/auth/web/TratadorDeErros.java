package com.orbitapay.auth.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.auth.domain.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.domain.exception.CredencialNaoEncontradaException;
import com.orbitapay.auth.domain.exception.PinIncorretoException;
import com.orbitapay.auth.domain.exception.RegraDeNegocioException;
import com.orbitapay.auth.web.dto.ErroResponse;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, ErroResponse.de("REGRA_DE_NEGOCIO", e.getMessage()));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> credenciaisInvalidas(CredenciaisInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, ErroResponse.de("CREDENCIAIS_INVALIDAS", e.getMessage()));
    }

    @ExceptionHandler(CredencialNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> naoEncontrada(CredencialNaoEncontradaException e) {
        return responder(HttpStatus.NOT_FOUND, ErroResponse.de("CREDENCIAL_NAO_ENCONTRADA", e.getMessage()));
    }

    @ExceptionHandler(PinIncorretoException.class)
    public ResponseEntity<ErroResponse> pinIncorreto(PinIncorretoException e) {
        return responder(HttpStatus.UNAUTHORIZED, new ErroResponse("PIN_INCORRETO", e.getMessage(),
                Map.of("tentativasRestantes", e.tentativasRestantes(), "bloqueado", e.bloqueado())));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> semSessao(MissingRequestHeaderException e) {
        return responder(HttpStatus.UNAUTHORIZED, ErroResponse.de("SESSAO_OBRIGATORIA", "Entre na sua conta"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException e) {
        return responder(HttpStatus.BAD_REQUEST, ErroResponse.de("REQUISICAO_INVALIDA", "Corpo da requisição inválido"));
    }

    private static ResponseEntity<ErroResponse> responder(HttpStatus status, ErroResponse erro) {
        return ResponseEntity.status(status).body(erro);
    }
}
