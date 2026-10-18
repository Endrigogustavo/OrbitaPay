package com.orbitapay.auth.adapter.controller;

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

import com.orbitapay.auth.adapter.controller.dto.request.AlteracaoDePinRequest;
import com.orbitapay.auth.adapter.controller.dto.request.LoginRequest;
import com.orbitapay.auth.adapter.controller.dto.response.CredencialCriadaResponse;
import com.orbitapay.auth.adapter.controller.dto.response.CredencialResponse;
import com.orbitapay.auth.adapter.controller.dto.response.SessaoResponse;
import com.orbitapay.auth.entity.Credencial;
import com.orbitapay.auth.entity.Sessao;
import com.orbitapay.auth.adapter.interfaces.ICookieDeSessao;
import com.orbitapay.auth.application.usecase.CredencialUseCase;
import com.orbitapay.auth.application.usecase.SessaoUseCase;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/autenticacao")
public class CredencialController {

    private final CredencialUseCase credencialUseCase;
    private final SessaoUseCase sessaoUseCase;
    private final ICookieDeSessao cookieDeSessao;

    public CredencialController(CredencialUseCase credencialUseCase, SessaoUseCase sessaoUseCase,
            ICookieDeSessao cookieDeSessao) {
        this.credencialUseCase = credencialUseCase;
        this.sessaoUseCase = sessaoUseCase;
        this.cookieDeSessao = cookieDeSessao;
    }

    @PostMapping("/credenciais")
    public ResponseEntity<?> cadastrarCredencial(@RequestBody LoginRequest request,
            @RequestHeader(value = "X-Perfil", required = false) String perfilDeQuemCadastra,
            HttpServletResponse resposta) {
        Credencial credencial = credencialUseCase.cadastrarCredencialDeNovoCliente(request.email(), request.pin());
        boolean cadastradoPelaEquipe = "GERENTE".equals(perfilDeQuemCadastra) || "ADMIN".equals(perfilDeQuemCadastra);
        if (cadastradoPelaEquipe) {
            return ResponseEntity.status(HttpStatus.CREATED).body(new CredencialCriadaResponse(credencial.clienteId()));
        }
        Sessao sessaoDoNovoCliente = sessaoUseCase.abrirSessaoDoCliente(credencial.clienteId());
        cookieDeSessao.gravarCookieDeSessaoNaResposta(resposta, sessaoDoNovoCliente.token());
        return ResponseEntity.status(HttpStatus.CREATED).body(SessaoResponse.criarAPartirDe(sessaoDoNovoCliente));
    }

    @DeleteMapping("/credenciais/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desfazerMeuCadastro(@RequestHeader("X-Cliente-Id") String clienteId, HttpServletResponse resposta) {
        credencialUseCase.desfazerCadastroFeitoHaPouco(clienteId);
        cookieDeSessao.apagarCookieDeSessaoDaResposta(resposta);
    }

    @DeleteMapping("/credenciais/{clienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desfazerCadastroFeitoPeloGerente(@PathVariable String clienteId) {
        credencialUseCase.desfazerCadastroFeitoHaPouco(clienteId);
    }

    @PutMapping("/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarPin(@RequestHeader("X-Cliente-Id") String clienteId,
            @RequestBody AlteracaoDePinRequest request) {
        credencialUseCase.alterarPinDoCliente(clienteId, request.pinAtual(), request.novoPin());
    }

    @GetMapping("/me")
    public CredencialResponse buscarMinhaCredencial(@RequestHeader("X-Cliente-Id") String clienteId) {
        return CredencialResponse.criarAPartirDe(credencialUseCase.buscarCredencialDoCliente(clienteId));
    }
}
