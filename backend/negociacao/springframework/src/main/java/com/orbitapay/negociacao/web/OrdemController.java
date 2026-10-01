package com.orbitapay.negociacao.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.negociacao.application.usecase.ConsultarOrdens;
import com.orbitapay.negociacao.application.usecase.EnviarOrdem;
import com.orbitapay.negociacao.domain.model.Ordem;
import com.orbitapay.negociacao.web.dto.OfertaResponse;
import com.orbitapay.negociacao.web.dto.OrdemRequest;
import com.orbitapay.negociacao.web.dto.OrdemResponse;

@RestController
public class OrdemController {

    private final EnviarOrdem enviarOrdem;
    private final ConsultarOrdens consultarOrdens;

    public OrdemController(EnviarOrdem enviarOrdem, ConsultarOrdens consultarOrdens) {
        this.enviarOrdem = enviarOrdem;
        this.consultarOrdens = consultarOrdens;
    }

    @PostMapping("/ordens")
    public ResponseEntity<OrdemResponse> enviar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody OrdemRequest requisicao) {
        Ordem ordem = enviarOrdem.executar(new EnviarOrdem.Comando(clienteId, requisicao.ticker(),
                requisicao.tipo(), requisicao.quantidade() == null ? 0 : requisicao.quantidade()));
        return ResponseEntity.accepted().location(URI.create("/ordens/" + ordem.id())).body(OrdemResponse.de(ordem));
    }

    @GetMapping("/ordens")
    public List<OrdemResponse> minhasOrdens(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return consultarOrdens.listarDoCliente(clienteId).stream().map(OrdemResponse::de).toList();
    }

    @GetMapping("/ordens/{ordemId}")
    public OrdemResponse buscar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @PathVariable String ordemId) {
        return OrdemResponse.de(consultarOrdens.buscarDoCliente(ordemId, clienteId));
    }

    @GetMapping("/ofertas/{ticker}")
    public OfertaResponse oferta(@PathVariable String ticker) {
        return OfertaResponse.de(consultarOrdens.oferta(ticker));
    }
}
