package com.orbitapay.clientes.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.orbitapay.clientes.adapter.in.web.dto.AtualizacaoRequest;
import com.orbitapay.clientes.adapter.in.web.dto.ClienteResponse;
import com.orbitapay.clientes.application.port.in.AtualizarDadosDoClienteUseCase;
import com.orbitapay.clientes.application.port.in.BloquearClienteUseCase;
import com.orbitapay.clientes.application.port.in.ConsultarClientesUseCase;
import com.orbitapay.clientes.application.port.in.DesbloquearClienteUseCase;
import com.orbitapay.clientes.application.port.in.RemoverClienteUseCase;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;

@RestController
@RequestMapping("/clientes")
public class GestaoDeClientesController {

    private final ConsultarClientesUseCase consultarClientes;
    private final AtualizarDadosDoClienteUseCase atualizarDados;
    private final BloquearClienteUseCase bloquearCliente;
    private final DesbloquearClienteUseCase desbloquearCliente;
    private final RemoverClienteUseCase removerCliente;

    public GestaoDeClientesController(ConsultarClientesUseCase consultarClientes,
            AtualizarDadosDoClienteUseCase atualizarDados, BloquearClienteUseCase bloquearCliente,
            DesbloquearClienteUseCase desbloquearCliente, RemoverClienteUseCase removerCliente) {
        this.consultarClientes = consultarClientes;
        this.atualizarDados = atualizarDados;
        this.bloquearCliente = bloquearCliente;
        this.desbloquearCliente = desbloquearCliente;
        this.removerCliente = removerCliente;
    }

    @GetMapping
    public List<ClienteResponse> listar(@RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return consultarClientes.listar().stream().map(ClienteResponse::de).toList();
    }

    @GetMapping("/{clienteId}")
    public ClienteResponse buscar(@PathVariable String clienteId,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return ClienteResponse.de(consultarClientes.buscar(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ClienteResponse atualizar(@PathVariable String clienteId, @RequestBody AtualizacaoRequest requisicao,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return ClienteResponse.de(atualizarDados.executar(new AtualizarDadosDoClienteUseCase.Comando(clienteId,
                requisicao.nome(), requisicao.email(), requisicao.cpf())));
    }

    @PostMapping("/{clienteId}/bloqueio")
    public ClienteResponse bloquear(@PathVariable String clienteId,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return ClienteResponse.de(bloquearCliente.executar(clienteId, MotivoBloqueio.GERENTE));
    }

    @PostMapping("/{clienteId}/desbloqueio")
    public ClienteResponse desbloquear(@PathVariable String clienteId,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        return ClienteResponse.de(desbloquearCliente.peloGerente(clienteId));
    }

    @DeleteMapping("/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable String clienteId,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        CabecalhosDoGateway.exigirGerente(perfil);
        removerCliente.executar(clienteId);
    }
}
