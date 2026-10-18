package com.orbitapay.auth.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.auth.adapter.controller.dto.request.GerenteRequest;
import com.orbitapay.auth.adapter.controller.dto.request.LoginRequest;
import com.orbitapay.auth.adapter.controller.dto.response.SessaoResponse;
import com.orbitapay.auth.entity.Sessao;
import com.orbitapay.auth.adapter.interfaces.ICookieDeSessao;
import com.orbitapay.auth.application.usecase.SessaoUseCase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/autenticacao")
public class SessaoController {

    private final SessaoUseCase sessaoUseCase;
    private final ICookieDeSessao cookieDeSessao;

    public SessaoController(SessaoUseCase sessaoUseCase, ICookieDeSessao cookieDeSessao) {
        this.sessaoUseCase = sessaoUseCase;
        this.cookieDeSessao = cookieDeSessao;
    }

    @PostMapping("/sessoes")
    public SessaoResponse entrar(@RequestBody LoginRequest request, HttpServletResponse resposta) {
        Sessao sessao = sessaoUseCase.entrarComEmailEPin(request.email(), request.pin());
        cookieDeSessao.gravarCookieDeSessaoNaResposta(resposta, sessao.token());
        return SessaoResponse.criarAPartirDe(sessao);
    }

    @GetMapping("/sessoes/atual")
    public SessaoResponse buscarSessaoAtual(HttpServletRequest requisicao) {
        String tokenDeSessao = cookieDeSessao.lerTokenDoCookieDeSessao(requisicao);
        return SessaoResponse.criarAPartirDe(sessaoUseCase.buscarSessaoAtiva(tokenDeSessao));
    }

    @DeleteMapping("/sessoes/atual")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(HttpServletRequest requisicao, HttpServletResponse resposta) {
        sessaoUseCase.encerrarSessao(cookieDeSessao.lerTokenDoCookieDeSessao(requisicao));
        cookieDeSessao.apagarCookieDeSessaoDaResposta(resposta);
    }

    @PostMapping("/me/gerente")
    public SessaoResponse tornarGerente(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody GerenteRequest request, HttpServletResponse resposta) {
        Sessao sessaoDoGerente = sessaoUseCase.transformarClienteEmGerente(clienteId, request.nome());
        cookieDeSessao.gravarCookieDeSessaoNaResposta(resposta, sessaoDoGerente.token());
        return SessaoResponse.criarAPartirDe(sessaoDoGerente);
    }
}
