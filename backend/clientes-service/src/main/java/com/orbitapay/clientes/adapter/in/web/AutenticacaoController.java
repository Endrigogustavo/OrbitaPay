package com.orbitapay.clientes.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.clientes.adapter.in.web.dto.CredencialResponse;
import com.orbitapay.clientes.adapter.in.web.dto.LoginClienteRequest;
import com.orbitapay.clientes.adapter.in.web.dto.LoginGerenteRequest;
import com.orbitapay.clientes.adapter.in.web.dto.SessaoResponse;
import com.orbitapay.clientes.application.port.in.AutenticarClienteUseCase;
import com.orbitapay.clientes.application.port.in.AutenticarGerenteUseCase;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoController {

    private final AutenticarClienteUseCase autenticarCliente;
    private final AutenticarGerenteUseCase autenticarGerente;

    public AutenticacaoController(AutenticarClienteUseCase autenticarCliente,
            AutenticarGerenteUseCase autenticarGerente) {
        this.autenticarCliente = autenticarCliente;
        this.autenticarGerente = autenticarGerente;
    }

    @PostMapping("/clientes")
    public SessaoResponse entrarComoCliente(@RequestBody LoginClienteRequest requisicao) {
        return SessaoResponse.de(autenticarCliente.executar(requisicao.email(), requisicao.pin()));
    }

    @PostMapping("/gerente")
    public CredencialResponse entrarComoGerente(@RequestBody LoginGerenteRequest requisicao) {
        return CredencialResponse.de(autenticarGerente.executar(requisicao.codigo()));
    }
}
