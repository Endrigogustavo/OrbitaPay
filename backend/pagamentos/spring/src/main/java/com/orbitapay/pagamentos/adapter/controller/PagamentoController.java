package com.orbitapay.pagamentos.adapter.controller;

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

import com.orbitapay.pagamentos.adapter.controller.dto.request.SolicitacaoDeDepositoRequest;
import com.orbitapay.pagamentos.adapter.controller.dto.response.PagamentoResponse;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.application.usecase.PagamentoUseCase;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoUseCase pagamentoUseCase;

    public PagamentoController(PagamentoUseCase pagamentoUseCase) {
        this.pagamentoUseCase = pagamentoUseCase;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> solicitarDeposito(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody SolicitacaoDeDepositoRequest request) {
        Pagamento pagamento = pagamentoUseCase.solicitarDeposito(clienteId, request.valor(), request.metodo());
        return ResponseEntity.created(URI.create("/pagamentos/" + pagamento.id()))
                .body(PagamentoResponse.criarAPartirDe(pagamento));
    }

    @GetMapping
    public List<PagamentoResponse> listarMeusPagamentos(@RequestHeader("X-Cliente-Id") String clienteId) {
        return pagamentoUseCase.listarPagamentosDoCliente(clienteId).stream()
                .map(PagamentoResponse::criarAPartirDe)
                .toList();
    }

    @GetMapping("/{pagamentoId}")
    public PagamentoResponse buscarPagamento(@RequestHeader("X-Cliente-Id") String clienteId,
            @PathVariable String pagamentoId) {
        return PagamentoResponse.criarAPartirDe(pagamentoUseCase.buscarPagamentoDoCliente(pagamentoId, clienteId));
    }
}
