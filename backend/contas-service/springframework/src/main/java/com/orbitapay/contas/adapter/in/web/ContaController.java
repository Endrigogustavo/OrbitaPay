package com.orbitapay.contas.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.contas.adapter.in.web.dto.ComprovanteResponse;
import com.orbitapay.contas.adapter.in.web.dto.ContaResponse;
import com.orbitapay.contas.adapter.in.web.dto.DepositoRequest;
import com.orbitapay.contas.adapter.in.web.dto.SaqueRequest;
import com.orbitapay.contas.application.port.in.ConsultarContasUseCase;
import com.orbitapay.contas.application.port.in.DepositarUseCase;
import com.orbitapay.contas.application.port.in.SacarUseCase;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ConsultarContasUseCase consultarContas;
    private final DepositarUseCase depositar;
    private final SacarUseCase sacar;

    public ContaController(ConsultarContasUseCase consultarContas, DepositarUseCase depositar, SacarUseCase sacar) {
        this.consultarContas = consultarContas;
        this.depositar = depositar;
        this.sacar = sacar;
    }

    @GetMapping("/me")
    public ContaResponse minhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return ContaResponse.de(consultarContas.porCliente(clienteId));
    }

    @PostMapping("/me/depositos")
    public ComprovanteResponse depositar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody DepositoRequest requisicao) {
        return ComprovanteResponse.de(depositar.executar(clienteId, requisicao.valor(), requisicao.metodo()));
    }

    @PostMapping("/me/saques")
    public ComprovanteResponse sacar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody SaqueRequest requisicao) {
        return ComprovanteResponse.de(sacar.executar(clienteId, requisicao.valor()));
    }

    @GetMapping
    public List<ContaResponse> listar(@RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return consultarContas.listar().stream().map(ContaResponse::de).toList();
    }
}
