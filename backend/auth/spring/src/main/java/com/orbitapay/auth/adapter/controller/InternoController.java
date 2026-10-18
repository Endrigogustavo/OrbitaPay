package com.orbitapay.auth.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.auth.adapter.controller.dto.request.EmailRequest;
import com.orbitapay.auth.adapter.controller.dto.response.AcessoResponse;
import com.orbitapay.auth.application.usecase.AssinaturaUseCase;
import com.orbitapay.auth.application.usecase.CredencialUseCase;

@RestController
@RequestMapping("/interno")
public class InternoController {

    private final AssinaturaUseCase assinaturaUseCase;
    private final CredencialUseCase credencialUseCase;

    public InternoController(AssinaturaUseCase assinaturaUseCase, CredencialUseCase credencialUseCase) {
        this.assinaturaUseCase = assinaturaUseCase;
        this.credencialUseCase = credencialUseCase;
    }

    @GetMapping("/acessos")
    public AcessoResponse consultarAcessoParaOGateway(
            @RequestHeader(value = "X-Sessao", required = false) String tokenDeSessao,
            @RequestHeader(value = "X-Assinatura", required = false) String tokenDeAssinatura) {
        return AcessoResponse.criarAPartirDe(
                assinaturaUseCase.consultarAcessoDaSessaoEDaAssinatura(tokenDeSessao, tokenDeAssinatura));
    }

    @PutMapping("/credenciais/{clienteId}/email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarEmailDeLogin(@PathVariable String clienteId, @RequestBody EmailRequest request) {
        credencialUseCase.atualizarEmailDeLoginDoCliente(clienteId, request.email());
    }
}
