package com.orbitapay.ativos.adapter.in.web;

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

import com.orbitapay.ativos.adapter.in.web.dto.AtivoRequest;
import com.orbitapay.ativos.adapter.in.web.dto.AtivoResponse;
import com.orbitapay.ativos.adapter.in.web.dto.BolsaResponse;
import com.orbitapay.ativos.application.port.in.AtualizarAtivoUseCase;
import com.orbitapay.ativos.application.port.in.ConsultarAtivosUseCase;
import com.orbitapay.ativos.application.port.in.ListarAtivoNaBolsaUseCase;
import com.orbitapay.ativos.application.port.in.RemoverAtivoUseCase;

@RestController
public class AtivoController {

    private final ConsultarAtivosUseCase consultarAtivos;
    private final ListarAtivoNaBolsaUseCase listarAtivo;
    private final AtualizarAtivoUseCase atualizarAtivo;
    private final RemoverAtivoUseCase removerAtivo;

    public AtivoController(ConsultarAtivosUseCase consultarAtivos, ListarAtivoNaBolsaUseCase listarAtivo,
            AtualizarAtivoUseCase atualizarAtivo, RemoverAtivoUseCase removerAtivo) {
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

    private static ListarAtivoNaBolsaUseCase.Comando comando(String ticker, AtivoRequest requisicao) {
        return new ListarAtivoNaBolsaUseCase.Comando(ticker, requisicao.nome(), requisicao.setor(),
                requisicao.bolsa(), requisicao.cotacao(), requisicao.quantidadeEmitida());
    }
}
