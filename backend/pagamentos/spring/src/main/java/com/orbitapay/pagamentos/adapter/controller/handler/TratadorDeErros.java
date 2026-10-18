package com.orbitapay.pagamentos.adapter.controller.handler;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.pagamentos.adapter.controller.dto.response.ErroResponse;
import com.orbitapay.pagamentos.application.exception.NaoEncontradoException;
import com.orbitapay.pagamentos.exception.RegraDeNegocioException;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(RegraDeNegocioException e) {
        return montarRespostaDeErro(HttpStatus.UNPROCESSABLE_CONTENT, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NaoEncontradoException e) {
        return montarRespostaDeErro(HttpStatus.NOT_FOUND, "PAGAMENTO_NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroResponse> tratarSessaoAusente(MissingRequestHeaderException e) {
        return montarRespostaDeErro(HttpStatus.UNAUTHORIZED, "SESSAO_OBRIGATORIA", "Entre na sua conta");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException e) {
        return montarRespostaDeErro(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Corpo da requisição inválido");
    }

    private static ResponseEntity<ErroResponse> montarRespostaDeErro(HttpStatus status, String codigo,
            String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(codigo, mensagem, Map.of()));
    }
}
