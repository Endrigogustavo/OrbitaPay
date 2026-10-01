package com.orbitapay.clientes.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.clientes.application.service.AutenticacaoIndisponivelException;
import com.orbitapay.clientes.domain.exception.ClienteNaoEncontradoException;
import com.orbitapay.clientes.domain.exception.RegraDeNegocioException;
import com.orbitapay.clientes.web.dto.ErroResponse;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException e) {
        return responder(HttpStatus.UNPROCESSABLE_CONTENT, ErroResponse.de("REGRA_DE_NEGOCIO", e.getMessage()));
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(ClienteNaoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, ErroResponse.de("CLIENTE_NAO_ENCONTRADO", e.getMessage()));
    }

    @ExceptionHandler(AutenticacaoIndisponivelException.class)
    public ResponseEntity<ErroResponse> autenticacaoIndisponivel(AutenticacaoIndisponivelException e) {
        return responder(HttpStatus.SERVICE_UNAVAILABLE, ErroResponse.de("AUTENTICACAO_INDISPONIVEL", e.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AcessoNegadoException e) {
        return responder(HttpStatus.FORBIDDEN, ErroResponse.de("ACESSO_NEGADO", e.getMessage()));
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
