package com.orbitapay.negociacao.adapter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.negociacao.adapter.controller.dto.response.CarteiraResponse;
import com.orbitapay.negociacao.application.usecase.CarteiraUseCase;

@RestController
@RequestMapping("/carteiras")
public class CarteiraController {

    private final CarteiraUseCase carteiraUseCase;

    public CarteiraController(CarteiraUseCase carteiraUseCase) {
        this.carteiraUseCase = carteiraUseCase;
    }

    @GetMapping("/me")
    public CarteiraResponse buscarMinhaCarteira(@RequestHeader("X-Cliente-Id") String clienteId) {
        return CarteiraResponse.criarAPartirDe(carteiraUseCase.consultarCarteiraValorizada(clienteId));
    }

    @GetMapping("/{clienteId}")
    public CarteiraResponse buscarCarteiraDoCliente(@PathVariable String clienteId) {
        return CarteiraResponse.criarAPartirDe(carteiraUseCase.consultarCarteiraValorizada(clienteId));
    }
}
