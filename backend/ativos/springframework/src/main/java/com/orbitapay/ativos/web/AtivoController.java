package com.orbitapay.ativos.web;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.ativos.application.usecase.AtualizarAtivo;
import com.orbitapay.ativos.application.usecase.ConsultarAtivos;
import com.orbitapay.ativos.application.usecase.ListarAtivoNaBolsa;
import com.orbitapay.ativos.application.usecase.RemoverAtivo;
import com.orbitapay.ativos.web.dto.AtivoRequest;
import com.orbitapay.ativos.web.dto.AtivoResponse;
import com.orbitapay.ativos.web.dto.BolsaResponse;

@RestController
public class AtivoController {

    private final ConsultarAtivos consultarAtivos;
    private final ListarAtivoNaBolsa listarAtivo;
    private final AtualizarAtivo atualizarAtivo;
    private final RemoverAtivo removerAtivo;

    public AtivoController(ConsultarAtivos consultarAtivos, ListarAtivoNaBolsa listarAtivo,
            AtualizarAtivo atualizarAtivo, RemoverAtivo removerAtivo) {
        this.consultarAtivos = consultarAtivos;
        this.listarAtivo = listarAtivo;
        this.atualizarAtivo = atualizarAtivo;
        this.removerAtivo = removerAtivo;
    }

    @GetMapping("/ativos")
    public List<AtivoResponse> listar() {
        return consultarAtivos.listar().stream().map(AtivoResponse::de).toList();
    }

    @GetMapping("/ativos/{ticker}")
    public AtivoResponse buscar(@PathVariable String ticker) {
        return AtivoResponse.de(consultarAtivos.buscar(ticker));
    }

    @PostMapping("/ativos")
    public ResponseEntity<AtivoResponse> listarNaBolsa(@RequestBody AtivoRequest requisicao,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        AtivoResponse resposta = AtivoResponse.de(listarAtivo.executar(comando(requisicao.ticker(), requisicao)));
        return ResponseEntity.created(URI.create("/ativos/" + resposta.ticker())).body(resposta);
    }

    @PutMapping("/ativos/{ticker}")
    public AtivoResponse atualizar(@PathVariable String ticker, @RequestBody AtivoRequest requisicao,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return AtivoResponse.de(atualizarAtivo.executar(comando(ticker, requisicao)));
    }

    @DeleteMapping("/ativos/{ticker}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable String ticker,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        removerAtivo.executar(ticker);
    }

    @GetMapping("/bolsas")
    public List<BolsaResponse> bolsas() {
        return consultarAtivos.bolsas().stream().map(BolsaResponse::de).toList();
    }

    private static ListarAtivoNaBolsa.Comando comando(String ticker, AtivoRequest requisicao) {
        return new ListarAtivoNaBolsa.Comando(ticker, requisicao.nome(), requisicao.setor(),
                requisicao.bolsa(), requisicao.cotacao(), requisicao.quantidadeEmitida());
    }
}
