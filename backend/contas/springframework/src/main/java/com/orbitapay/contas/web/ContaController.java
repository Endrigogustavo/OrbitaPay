package com.orbitapay.contas.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.contas.application.usecase.ConsultarContas;
import com.orbitapay.contas.application.usecase.Sacar;
import com.orbitapay.contas.web.dto.ComprovanteResponse;
import com.orbitapay.contas.web.dto.ContaResponse;
import com.orbitapay.contas.web.dto.SaqueRequest;

/**
 * Depósitos não entram por aqui: o cliente gera uma cobrança no contexto de Pagamentos e o crédito chega pelo
 * evento {@code pagamento.confirmado}.
 */
@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ConsultarContas consultarContas;
    private final Sacar sacar;

    public ContaController(ConsultarContas consultarContas, Sacar sacar) {
        this.consultarContas = consultarContas;
        this.sacar = sacar;
    }

    @GetMapping("/me")
    public ContaResponse minhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return ContaResponse.de(consultarContas.porCliente(clienteId));
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
