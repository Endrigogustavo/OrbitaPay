package com.orbitapay.pagamentos.web;

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

import com.orbitapay.pagamentos.application.usecase.ConsultarPagamentos;
import com.orbitapay.pagamentos.application.usecase.SolicitarPagamento;
import com.orbitapay.pagamentos.domain.model.Pagamento;
import com.orbitapay.pagamentos.web.dto.PagamentoRequest;
import com.orbitapay.pagamentos.web.dto.PagamentoResponse;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final SolicitarPagamento solicitarPagamento;
    private final ConsultarPagamentos consultarPagamentos;

    public PagamentoController(SolicitarPagamento solicitarPagamento, ConsultarPagamentos consultarPagamentos) {
        this.solicitarPagamento = solicitarPagamento;
        this.consultarPagamentos = consultarPagamentos;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> solicitar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody PagamentoRequest requisicao) {
        Pagamento pagamento = solicitarPagamento.executar(new SolicitarPagamento.Comando(clienteId, requisicao.valor(),
                requisicao.metodo()));
        return ResponseEntity.created(URI.create("/pagamentos/" + pagamento.id())).body(PagamentoResponse.de(pagamento));
    }

    @GetMapping
    public List<PagamentoResponse> meusPagamentos(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return consultarPagamentos.listarDoCliente(clienteId).stream().map(PagamentoResponse::de).toList();
    }

    @GetMapping("/{pagamentoId}")
    public PagamentoResponse buscar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @PathVariable String pagamentoId) {
        return PagamentoResponse.de(consultarPagamentos.doCliente(pagamentoId, clienteId));
    }
}
