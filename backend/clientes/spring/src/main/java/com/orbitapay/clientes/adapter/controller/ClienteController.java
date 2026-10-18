package com.orbitapay.clientes.adapter.controller;

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

import com.orbitapay.clientes.adapter.controller.dto.request.AtualizacaoDeClienteRequest;
import com.orbitapay.clientes.adapter.controller.dto.request.CadastroDeClienteRequest;
import com.orbitapay.clientes.adapter.controller.dto.response.ClienteResponse;
import com.orbitapay.clientes.entity.Cliente;
import com.orbitapay.clientes.entity.MotivoBloqueio;
import com.orbitapay.clientes.application.usecase.ClienteUseCase;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrarCliente(@RequestBody CadastroDeClienteRequest request,
            @RequestHeader(value = "X-Cliente-Id", required = false) String clienteDaSessao,
            @RequestHeader(value = "X-Perfil", required = false) String perfilDeQuemCadastra) {
        boolean cadastradoPelaEquipe = "GERENTE".equals(perfilDeQuemCadastra) || "ADMIN".equals(perfilDeQuemCadastra);
        String clienteId = cadastradoPelaEquipe ? request.clienteId() : clienteDaSessao;
        Cliente cliente = clienteUseCase.cadastrarClienteEAbrirConta(clienteId, request.nome(), request.email(),
                request.cpf(), cadastradoPelaEquipe ? request.depositoInicial() : null);
        return ResponseEntity.created(URI.create("/clientes/" + cliente.id()))
                .body(ClienteResponse.criarAPartirDe(cliente));
    }

    @GetMapping("/me")
    public ClienteResponse buscarMeuCadastro(@RequestHeader("X-Cliente-Id") String clienteId) {
        return ClienteResponse.criarAPartirDe(clienteUseCase.buscarCliente(clienteId));
    }

    @PutMapping("/me")
    public ClienteResponse atualizarMeusDados(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody AtualizacaoDeClienteRequest request) {
        return ClienteResponse.criarAPartirDe(
                clienteUseCase.atualizarDadosDoCliente(clienteId, request.nome(), request.email(), null));
    }

    @PostMapping("/me/bloqueio")
    public ClienteResponse bloquearMinhaConta(@RequestHeader("X-Cliente-Id") String clienteId) {
        return ClienteResponse.criarAPartirDe(clienteUseCase.bloquearCliente(clienteId, MotivoBloqueio.CLIENTE));
    }

    @PostMapping("/me/desbloqueio")
    public ClienteResponse desbloquearMinhaConta(@RequestHeader("X-Cliente-Id") String clienteId) {
        return ClienteResponse.criarAPartirDe(clienteUseCase.desbloquearPeloProprioCliente(clienteId));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrarMinhaConta(@RequestHeader("X-Cliente-Id") String clienteId) {
        clienteUseCase.removerClienteESuaConta(clienteId);
    }

    @GetMapping
    public List<ClienteResponse> listarClientes() {
        return clienteUseCase.listarClientes().stream().map(ClienteResponse::criarAPartirDe).toList();
    }

    @GetMapping("/{clienteId}")
    public ClienteResponse buscarCliente(@PathVariable String clienteId) {
        return ClienteResponse.criarAPartirDe(clienteUseCase.buscarCliente(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ClienteResponse atualizarCliente(@PathVariable String clienteId,
            @RequestBody AtualizacaoDeClienteRequest request) {
        return ClienteResponse.criarAPartirDe(
                clienteUseCase.atualizarDadosDoCliente(clienteId, request.nome(), request.email(), request.cpf()));
    }

    @PostMapping("/{clienteId}/bloqueio")
    public ClienteResponse bloquearClientePeloGerente(@PathVariable String clienteId) {
        return ClienteResponse.criarAPartirDe(clienteUseCase.bloquearCliente(clienteId, MotivoBloqueio.GERENTE));
    }

    @PostMapping("/{clienteId}/desbloqueio")
    public ClienteResponse desbloquearClientePeloGerente(@PathVariable String clienteId) {
        return ClienteResponse.criarAPartirDe(clienteUseCase.desbloquearPeloGerente(clienteId));
    }

    @DeleteMapping("/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerCliente(@PathVariable String clienteId) {
        clienteUseCase.removerClienteESuaConta(clienteId);
    }
}
