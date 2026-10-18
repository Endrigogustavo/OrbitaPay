package com.orbitapay.auth.adapter.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.auth.adapter.controller.dto.request.PinRequest;
import com.orbitapay.auth.adapter.controller.dto.response.AssinaturaResponse;
import com.orbitapay.auth.application.usecase.AssinaturaUseCase;

@RestController
@RequestMapping("/autenticacao")
public class AssinaturaController {

    private final AssinaturaUseCase assinaturaUseCase;

    public AssinaturaController(AssinaturaUseCase assinaturaUseCase) {
        this.assinaturaUseCase = assinaturaUseCase;
    }

    @PostMapping("/assinaturas")
    public AssinaturaResponse assinarOperacaoComPin(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody PinRequest request) {
        return AssinaturaResponse.criarAPartirDe(assinaturaUseCase.assinarOperacaoComPin(clienteId, request.pin()));
    }
}
