package com.orbitapay.auth.adapter.controller.handler;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbitapay.auth.adapter.controller.dto.response.ErroResponse;
import com.orbitapay.auth.application.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.application.exception.NaoEncontradoException;
import com.orbitapay.auth.application.exception.PinIncorretoException;
import com.orbitapay.auth.exception.RegraDeNegocioException;
import com.orbitapay.auth.adapter.gateway.exception.ServicoIndisponivelException;
import com.orbitapay.auth.application.exception.SessaoInvalidaException;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraDeNegocio(RegraDeNegocioException e) {
        return montarRespostaDeErro(HttpStatus.UNPROCESSABLE_CONTENT, "REGRA_DE_NEGOCIO", e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException e) {
        return montarRespostaDeErro(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", e.getMessage());
    }

    @ExceptionHandler(PinIncorretoException.class)
    public ResponseEntity<ErroResponse> tratarPinIncorreto(PinIncorretoException e) {
        Map<String, Object> detalhes = Map.of("tentativasRestantes", e.tentativasRestantes(), "bloqueado", e.contaFoiBloqueada());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErroResponse("PIN_INCORRETO", e.getMessage(), detalhes));
    }

    @ExceptionHandler(NaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(NaoEncontradoException e) {
        return montarRespostaDeErro(HttpStatus.NOT_FOUND, "CREDENCIAL_NAO_ENCONTRADA", e.getMessage());
    }

    @ExceptionHandler({ SessaoInvalidaException.class, MissingRequestHeaderException.class })
    public ResponseEntity<ErroResponse> tratarSessaoAusente(Exception e) {
        return montarRespostaDeErro(HttpStatus.UNAUTHORIZED, "SESSAO_OBRIGATORIA", "Entre na sua conta");
    }

    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarServicoIndisponivel(ServicoIndisponivelException e) {
        return montarRespostaDeErro(HttpStatus.SERVICE_UNAVAILABLE, "SERVICO_INDISPONIVEL", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException e) {
        return montarRespostaDeErro(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", "Corpo da requisição inválido");
    }

    private static ResponseEntity<ErroResponse> montarRespostaDeErro(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(codigo, mensagem, Map.of()));
    }
}
