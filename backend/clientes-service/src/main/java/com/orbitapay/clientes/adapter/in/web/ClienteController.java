package com.orbitapay.clientes.adapter.in.web;

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

import com.orbitapay.clientes.adapter.in.web.dto.AlteracaoPinRequest;
import com.orbitapay.clientes.adapter.in.web.dto.AtualizacaoRequest;
import com.orbitapay.clientes.adapter.in.web.dto.CadastroRequest;
import com.orbitapay.clientes.adapter.in.web.dto.ClienteResponse;
import com.orbitapay.clientes.adapter.in.web.dto.CredencialResponse;
import com.orbitapay.clientes.adapter.in.web.dto.PinRequest;
import com.orbitapay.clientes.application.port.in.AlterarPinUseCase;
import com.orbitapay.clientes.application.port.in.AssinarOperacaoUseCase;
import com.orbitapay.clientes.application.port.in.AtualizarDadosDoClienteUseCase;
import com.orbitapay.clientes.application.port.in.BloquearClienteUseCase;
import com.orbitapay.clientes.application.port.in.CadastrarClienteUseCase;
import com.orbitapay.clientes.application.port.in.ConsultarClientesUseCase;
import com.orbitapay.clientes.application.port.in.DesbloquearClienteUseCase;
import com.orbitapay.clientes.application.port.in.RemoverClienteUseCase;
import com.orbitapay.clientes.domain.model.Cliente;
import com.orbitapay.clientes.domain.model.MotivoBloqueio;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final CadastrarClienteUseCase cadastrarCliente;
    private final ConsultarClientesUseCase consultarClientes;
    private final AtualizarDadosDoClienteUseCase atualizarDados;
    private final AlterarPinUseCase alterarPin;
    private final AssinarOperacaoUseCase assinarOperacao;
    private final BloquearClienteUseCase bloquearCliente;
    private final DesbloquearClienteUseCase desbloquearCliente;
    private final RemoverClienteUseCase removerCliente;

    public ClienteController(CadastrarClienteUseCase cadastrarCliente, ConsultarClientesUseCase consultarClientes,
            AtualizarDadosDoClienteUseCase atualizarDados, AlterarPinUseCase alterarPin,
            AssinarOperacaoUseCase assinarOperacao, BloquearClienteUseCase bloquearCliente,
            DesbloquearClienteUseCase desbloquearCliente, RemoverClienteUseCase removerCliente) {
        this.cadastrarCliente = cadastrarCliente;
        this.consultarClientes = consultarClientes;
        this.atualizarDados = atualizarDados;
        this.alterarPin = alterarPin;
        this.assinarOperacao = assinarOperacao;
        this.bloquearCliente = bloquearCliente;
        this.desbloquearCliente = desbloquearCliente;
        this.removerCliente = removerCliente;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> cadastrar(@RequestBody CadastroRequest requisicao,
            @RequestHeader(value = CabecalhosDoGateway.PERFIL, required = false) String perfil) {
        boolean gerente = CabecalhosDoGateway.PERFIL_GERENTE.equals(perfil);
        Cliente cliente = cadastrarCliente.executar(new CadastrarClienteUseCase.Comando(requisicao.nome(),
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
        return ClienteResponse.de(atualizarDados.executar(new AtualizarDadosDoClienteUseCase.Comando(clienteId,
                requisicao.nome(), requisicao.email(), null)));
    }

    @PutMapping("/me/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarMeuPin(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody AlteracaoPinRequest requisicao) {
        alterarPin.executar(clienteId, requisicao.pinAtual(), requisicao.novoPin());
    }

    @PostMapping("/me/assinaturas")
    public CredencialResponse assinar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody PinRequest requisicao) {
        return CredencialResponse.de(assinarOperacao.executar(clienteId, requisicao.pin()));
    }

    @PostMapping("/me/bloqueio")
    public ClienteResponse bloquearMinhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return ClienteResponse.de(bloquearCliente.executar(clienteId, MotivoBloqueio.CLIENTE));
    }

    @PostMapping("/me/desbloqueio")
    public ClienteResponse desbloquearMinhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody PinRequest requisicao) {
        return ClienteResponse.de(desbloquearCliente.peloCliente(clienteId, requisicao.pin()));
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void encerrarMinhaConta(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        removerCliente.executar(clienteId);
    }
}
