package com.orbitapay.clientes.web;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.clientes.application.usecase.AtualizarDadosDoCliente;
import com.orbitapay.clientes.application.usecase.BloquearCliente;
import com.orbitapay.clientes.application.usecase.CadastrarCliente;
import com.orbitapay.clientes.application.usecase.ConsultarClientes;
import com.orbitapay.clientes.application.usecase.DesbloquearPeloCliente;
import com.orbitapay.clientes.application.usecase.RemoverCliente;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;
import com.orbitapay.clientes.web.dto.AtualizacaoRequest;
import com.orbitapay.clientes.web.dto.CadastroRequest;
import com.orbitapay.clientes.web.dto.ClienteResponse;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final CadastrarCliente cadastrarCliente;
    private final ConsultarClientes consultarClientes;
    private final AtualizarDadosDoCliente atualizarDados;
    private final BloquearCliente bloquearCliente;
    private final DesbloquearPeloCliente desbloquearPeloCliente;
    private final RemoverCliente removerCliente;

    public ClienteController(CadastrarCliente cadastrarCliente, ConsultarClientes consultarClientes,
            AtualizarDadosDoCliente atualizarDados, BloquearCliente bloquearCliente,
            DesbloquearPeloCliente desbloquearPeloCliente, RemoverCliente removerCliente) {
        this.cadastrarCliente = cadastrarCliente;
        this.consultarClientes = consultarClientes;
        this.atualizarDados = atualizarDados;
        this.bloquearCliente = bloquearCliente;
        this.desbloquearPeloCliente = desbloquearPeloCliente;
        this.removerCliente = removerCliente;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody CadastroRequest requisicao,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        boolean gerente = CabecalhosDoGateway.PERFIL_GERENTE.equals(perfil);
        Cliente cliente = cadastrarCliente.executar(new CadastrarCliente.Comando(requisicao.nome(),
                requisicao.email(), requisicao.cpf(), requisicao.pin(), gerente ? requisicao.depositoInicial() : null));
        return ResponseEntity.created(URI.create("/clientes/" + cliente.id())).body(ClienteResponse.de(cliente));
    }

    @GetMapping("/me")
    public ClienteResponse meuCadastro(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return ClienteResponse.de(consultarClientes.buscar(clienteId));
    }

    @PutMapping("/me")
    public ClienteResponse atualizarMeusDados(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody AtualizacaoRequest requisicao) {
        return ClienteResponse.de(atualizarDados.executar(new AtualizarDadosDoCliente.Comando(clienteId,
                requisicao.nome(), requisicao.email(), null)));
    }

    @PostMapping("/me/bloqueio")
    public ClienteResponse bloquearMinhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return ClienteResponse.de(bloquearCliente.executar(clienteId, MotivoBloqueio.CLIENTE));
    }

    @PostMapping("/me/desbloqueio")
    public ClienteResponse desbloquearMinhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return ClienteResponse.de(desbloquearPeloCliente.executar(clienteId));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrarMinhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        removerCliente.executar(clienteId);
    }
}
