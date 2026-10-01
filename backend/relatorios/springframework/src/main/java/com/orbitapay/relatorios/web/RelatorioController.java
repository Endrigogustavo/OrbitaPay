package com.orbitapay.relatorios.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.relatorios.application.dto.ExtratoDeInvestimentos;
import com.orbitapay.relatorios.application.dto.RelatorioGerencial;
import com.orbitapay.relatorios.application.usecase.GerarExtratoDeInvestimentos;
import com.orbitapay.relatorios.application.usecase.GerarRelatorioGerencial;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final GerarRelatorioGerencial gerarRelatorioGerencial;
    private final GerarExtratoDeInvestimentos gerarExtrato;

    public RelatorioController(GerarRelatorioGerencial gerarRelatorioGerencial,
            GerarExtratoDeInvestimentos gerarExtrato) {
        this.gerarRelatorioGerencial = gerarRelatorioGerencial;
        this.gerarExtrato = gerarExtrato;
    }

    @GetMapping("/gerencial")
    public RelatorioGerencial gerencial(
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return gerarRelatorioGerencial.executar(de, ate);
    }

    @GetMapping("/me")
    public ExtratoDeInvestimentos meuExtrato(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return gerarExtrato.executar(clienteId, de, ate);
    }
}
