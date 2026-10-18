package com.orbitapay.negociacao.adapter.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.negociacao.adapter.controller.dto.request.OrdemRequest;
import com.orbitapay.negociacao.adapter.controller.dto.response.OrdemResponse;
import com.orbitapay.negociacao.entity.Ordem;
import com.orbitapay.negociacao.application.usecase.OrdemUseCase;

@RestController
@RequestMapping("/ordens")
public class OrdemController {

    private final OrdemUseCase ordemUseCase;

    public OrdemController(OrdemUseCase ordemUseCase) {
        this.ordemUseCase = ordemUseCase;
    }

    @PostMapping
    public ResponseEntity<OrdemResponse> solicitarOrdem(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody OrdemRequest request) {
        Ordem ordemPendente = ordemUseCase.solicitarOrdem(clienteId, request.ticker(), request.tipo(),
                request.quantidade());
        return ResponseEntity.accepted().location(URI.create("/ordens/" + ordemPendente.id()))
                .body(OrdemResponse.criarAPartirDe(ordemPendente));
    }

    @GetMapping
    public List<OrdemResponse> listarMinhasOrdens(@RequestHeader("X-Cliente-Id") String clienteId) {
        return ordemUseCase.listarOrdensDoCliente(clienteId).stream().map(OrdemResponse::criarAPartirDe).toList();
    }

    @GetMapping("/{ordemId}")
    public OrdemResponse buscarOrdem(@RequestHeader("X-Cliente-Id") String clienteId, @PathVariable String ordemId) {
        return OrdemResponse.criarAPartirDe(ordemUseCase.buscarOrdemDoCliente(ordemId, clienteId));
    }
}
