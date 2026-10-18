package com.orbitapay.clientes.adapter.controller.handler;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.clientes.adapter.controller.dto.response.ErroResponse;
import com.orbitapay.clientes.adapter.gateway.exception.ServicoIndisponivelException;
import com.orbitapay.clientes.exception.ContaBloqueadaException;
import com.orbitapay.clientes.application.exception.NaoEncontradoException;
import com.orbitapay.clientes.exception.RegraDeNegocioException;
import com.orbitapay.clientes.exception.SaldoInsuficienteException;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(RegraDeNegocioException e) {
        return montarRespostaDeErro(HttpStatus.UNPROCESSABLE_CONTENT, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<ErroResponse> tratarSaldoInsuficiente(SaldoInsuficienteException e) {
        return montarRespostaDeErro(HttpStatus.UNPROCESSABLE_CONTENT, "SALDO_INSUFICIENTE", e.getMessage());
    }

    @ExceptionHandler(ContaBloqueadaException.class)
    public ResponseEntity<ErroResponse> tratarContaBloqueada(ContaBloqueadaException e) {
        return montarRespostaDeErro(HttpStatus.LOCKED, "CONTA_BLOQUEADA", e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NaoEncontradoException e) {
        return montarRespostaDeErro(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", e.getMessage());
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarServicoIndisponivel(ServicoIndisponivelException e) {
        return montarRespostaDeErro(HttpStatus.SERVICE_UNAVAILABLE, "SERVICO_INDISPONIVEL", e.getMessage());
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
