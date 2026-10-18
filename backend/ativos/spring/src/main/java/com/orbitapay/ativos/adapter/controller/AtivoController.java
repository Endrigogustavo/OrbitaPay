package com.orbitapay.ativos.adapter.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.ativos.adapter.controller.dto.request.AtivoRequest;
import com.orbitapay.ativos.adapter.controller.dto.response.AtivoResponse;
import com.orbitapay.ativos.adapter.controller.dto.response.BolsaResponse;
import com.orbitapay.ativos.application.usecase.AtivoUseCase;

@RestController
public class AtivoController {

    private final AtivoUseCase ativoUseCase;

    public AtivoController(AtivoUseCase ativoUseCase) {
        this.ativoUseCase = ativoUseCase;
    }

    @GetMapping("/ativos")
    public List<AtivoResponse> listarAtivos() {
        return ativoUseCase.listarAtivosComSuasBolsas().stream().map(AtivoResponse::criarAPartirDe).toList();
    }

    @GetMapping("/ativos/{ticker}")
    public AtivoResponse buscarAtivo(@PathVariable String ticker) {
        return AtivoResponse.criarAPartirDe(ativoUseCase.buscarAtivoComSuaBolsa(ticker));
    }

    @PostMapping("/ativos")
    public ResponseEntity<AtivoResponse> listarNovoAtivo(@RequestBody AtivoRequest request) {
        AtivoResponse ativo = AtivoResponse.criarAPartirDe(ativoUseCase.listarNovoAtivoNaBolsa(request.ticker(), request.nome(),
                request.setor(), request.bolsa(), request.cotacao(), request.quantidadeEmitida()));
        return ResponseEntity.created(URI.create("/ativos/" + ativo.ticker())).body(ativo);
    }

    @PutMapping("/ativos/{ticker}")
    public AtivoResponse atualizarAtivo(@PathVariable String ticker, @RequestBody AtivoRequest request) {
        return AtivoResponse.criarAPartirDe(ativoUseCase.atualizarCadastroDoAtivo(ticker, request.nome(), request.setor(),
                request.bolsa(), request.cotacao(), request.quantidadeEmitida()));
    }

    @DeleteMapping("/ativos/{ticker}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerAtivo(@PathVariable String ticker) {
        ativoUseCase.removerAtivoDaBolsa(ticker);
    }

    @GetMapping("/bolsas")
    public List<BolsaResponse> listarBolsas() {
        return ativoUseCase.listarBolsas().stream().map(BolsaResponse::criarAPartirDe).toList();
    }
}
