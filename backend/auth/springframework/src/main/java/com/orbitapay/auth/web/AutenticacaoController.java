package com.orbitapay.auth.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.auth.application.usecase.AlterarPin;
import com.orbitapay.auth.application.usecase.AssinarOperacao;
import com.orbitapay.auth.application.usecase.AutenticarCliente;
import com.orbitapay.auth.application.usecase.AutenticarGerente;
import com.orbitapay.auth.application.usecase.ConsultarCredencial;
import com.orbitapay.auth.web.dto.AlteracaoPinRequest;
import com.orbitapay.auth.web.dto.CredencialResponse;
import com.orbitapay.auth.web.dto.LoginClienteRequest;
import com.orbitapay.auth.web.dto.LoginGerenteRequest;
import com.orbitapay.auth.web.dto.PinRequest;
import com.orbitapay.auth.web.dto.SessaoResponse;
import com.orbitapay.auth.web.dto.TokenResponse;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoController {

    private final AutenticarCliente autenticarCliente;
    private final AutenticarGerente autenticarGerente;
    private final AssinarOperacao assinarOperacao;
    private final AlterarPin alterarPin;
    private final ConsultarCredencial consultarCredencial;

    public AutenticacaoController(AutenticarCliente autenticarCliente, AutenticarGerente autenticarGerente,
            AssinarOperacao assinarOperacao, AlterarPin alterarPin, ConsultarCredencial consultarCredencial) {
        this.autenticarCliente = autenticarCliente;
        this.autenticarGerente = autenticarGerente;
        this.assinarOperacao = assinarOperacao;
        this.alterarPin = alterarPin;
        this.consultarCredencial = consultarCredencial;
    }

    @PostMapping("/clientes")
    public SessaoResponse entrarComoCliente(@RequestBody LoginClienteRequest requisicao) {
        return SessaoResponse.de(autenticarCliente.executar(requisicao.email(), requisicao.pin()));
    }

    @PostMapping("/gerente")
    public TokenResponse entrarComoGerente(@RequestBody LoginGerenteRequest requisicao) {
        return TokenResponse.de(autenticarGerente.executar(requisicao.codigo()));
    }

    @PostMapping("/assinaturas")
    public TokenResponse assinar(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody PinRequest requisicao) {
        return TokenResponse.de(assinarOperacao.executar(clienteId, requisicao.pin()));
    }

    @PutMapping("/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarPin(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId,
            @RequestBody AlteracaoPinRequest requisicao) {
        alterarPin.executar(clienteId, requisicao.pinAtual(), requisicao.novoPin());
    }

    @GetMapping("/me")
    public CredencialResponse minhaCredencial(@RequestHeader(CabecalhosDoGateway.CLIENTE_ID) String clienteId) {
        return CredencialResponse.de(consultarCredencial.doCliente(clienteId));
    }
}
