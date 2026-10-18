package com.orbitapay.clientes.adapter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.clientes.adapter.controller.dto.request.SaqueRequest;
import com.orbitapay.clientes.adapter.controller.dto.response.ComprovanteResponse;
import com.orbitapay.clientes.adapter.controller.dto.response.ContaResponse;
import com.orbitapay.clientes.application.usecase.ContaUseCase;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaUseCase contaUseCase;

    public ContaController(ContaUseCase contaUseCase) {
        this.contaUseCase = contaUseCase;
    }

    @GetMapping("/me")
    public ContaResponse buscarMinhaConta(@RequestHeader("X-Cliente-Id") String clienteId) {
        return ContaResponse.criarAPartirDe(contaUseCase.buscarContaDoCliente(clienteId));
    }

    @PostMapping("/me/saques")
    public ComprovanteResponse sacar(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody SaqueRequest request) {
        return ComprovanteResponse.criarAPartirDe(contaUseCase.sacarDinheiroDaConta(clienteId, request.valor()));
    }

    @GetMapping
    public List<ContaResponse> listarContas() {
        return contaUseCase.listarContas().stream().map(ContaResponse::criarAPartirDe).toList();
    }
}
