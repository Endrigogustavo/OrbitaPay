package com.orbitapay.ativos.adapter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.ativos.adapter.controller.dto.request.MovimentoDeEstoqueRequest;
import com.orbitapay.ativos.application.usecase.AtivoUseCase;

@RestController
@RequestMapping("/interno/ativos/{ticker}/estoque")
public class EstoqueController {

    private final AtivoUseCase ativoUseCase;

    public EstoqueController(AtivoUseCase ativoUseCase) {
        this.ativoUseCase = ativoUseCase;
    }

    @PostMapping("/retiradas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirarAcoesDoEstoque(@PathVariable String ticker, @RequestBody MovimentoDeEstoqueRequest request) {
        ativoUseCase.retirarAcoesDoEstoque(ticker, request.quantidade());
    }

    @PostMapping("/devolucoes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void devolverAcoesAoEstoque(@PathVariable String ticker, @RequestBody MovimentoDeEstoqueRequest request) {
        ativoUseCase.devolverAcoesAoEstoque(ticker, request.quantidade());
    }
}
