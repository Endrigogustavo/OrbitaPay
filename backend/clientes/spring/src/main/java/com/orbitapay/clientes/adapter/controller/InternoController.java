package com.orbitapay.clientes.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.clientes.adapter.controller.dto.request.MovimentoDeOrdemRequest;
import com.orbitapay.clientes.adapter.controller.dto.response.SituacaoDoClienteResponse;
import com.orbitapay.clientes.application.usecase.ClienteUseCase;
import com.orbitapay.clientes.application.usecase.ContaUseCase;

@RestController
@RequestMapping("/interno")
public class InternoController {

    private final ClienteUseCase clienteUseCase;
    private final ContaUseCase contaUseCase;

    public InternoController(ClienteUseCase clienteUseCase, ContaUseCase contaUseCase) {
        this.clienteUseCase = clienteUseCase;
        this.contaUseCase = contaUseCase;
    }

    @GetMapping("/clientes/{clienteId}/situacao")
    public SituacaoDoClienteResponse buscarSituacaoDoCliente(@PathVariable String clienteId) {
        return new SituacaoDoClienteResponse(clienteId, clienteUseCase.clienteEstaBloqueado(clienteId));
    }

    @PostMapping("/clientes/{clienteId}/bloqueio-por-pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void bloquearPorErrosDePin(@PathVariable String clienteId) {
        clienteUseCase.bloquearClientePorErrosDePin(clienteId);
    }

    @PostMapping("/contas/{clienteId}/compras")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void debitarCompraDeAcoes(@PathVariable String clienteId, @RequestBody MovimentoDeOrdemRequest request) {
        contaUseCase.debitarCompraDeAcoes(clienteId, request.ordemId(), request.valor(), request.descricao());
    }

    @PostMapping("/contas/{clienteId}/compras/{ordemId}/estorno")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void estornarCompraDeAcoes(@PathVariable String clienteId, @PathVariable String ordemId) {
        contaUseCase.estornarCompraDeAcoes(clienteId, ordemId);
    }

    @PostMapping("/contas/{clienteId}/vendas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void creditarVendaDeAcoes(@PathVariable String clienteId, @RequestBody MovimentoDeOrdemRequest request) {
        contaUseCase.creditarVendaDeAcoes(clienteId, request.ordemId(), request.valor(), request.descricao());
    }
}
