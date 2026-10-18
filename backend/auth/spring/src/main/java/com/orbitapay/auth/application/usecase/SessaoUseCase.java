package com.orbitapay.auth.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.orbitapay.auth.application.exception.CredenciaisInvalidasException;
import com.orbitapay.auth.application.exception.SessaoInvalidaException;
import com.orbitapay.auth.application.gateway.ICodificadorDePinGateway;
import com.orbitapay.auth.application.gateway.ICredencialGateway;
import com.orbitapay.auth.application.gateway.IFuncionarioGateway;
import com.orbitapay.auth.application.gateway.ISessaoGateway;
import com.orbitapay.auth.entity.Credencial;
import com.orbitapay.auth.entity.Funcionario;
import com.orbitapay.auth.entity.Perfil;
import com.orbitapay.auth.entity.Sessao;
import com.orbitapay.auth.service.CredencialService;

public class SessaoUseCase {

    private final CredencialService credencialService;
    private final CredencialUseCase credencialUseCase;
    private final ICredencialGateway credencialGateway;
    private final IFuncionarioGateway funcionarioGateway;
    private final ISessaoGateway sessaoGateway;
    private final ICodificadorDePinGateway codificadorDePinGateway;
    private final Duration duracaoDaSessao;

    public SessaoUseCase(CredencialService credencialService, CredencialUseCase credencialUseCase,
            ICredencialGateway credencialGateway, IFuncionarioGateway funcionarioGateway, ISessaoGateway sessaoGateway,
            ICodificadorDePinGateway codificadorDePinGateway, Duration duracaoDaSessao) {
        this.credencialService = credencialService;
        this.credencialUseCase = credencialUseCase;
        this.credencialGateway = credencialGateway;
        this.funcionarioGateway = funcionarioGateway;
        this.sessaoGateway = sessaoGateway;
        this.codificadorDePinGateway = codificadorDePinGateway;
        this.duracaoDaSessao = duracaoDaSessao;
    }

    public Sessao entrarComEmailEPin(String email, String pin) {
        if (email == null || email.isBlank()) {
            throw new CredenciaisInvalidasException("Informe seu e-mail");
        }
        String emailNormalizado = credencialService.validarENormalizarEmail(email);
        credencialService.validarFormatoDoPin(pin);

        Optional<Funcionario> funcionario = funcionarioGateway.buscarPorEmail(emailNormalizado);
        if (funcionario.isPresent()) {
            if (!codificadorDePinGateway.pinConfereComOCodificado(pin, funcionario.get().pinCodificado())) {
                throw new CredenciaisInvalidasException("PIN incorreto");
            }
            return abrirSessaoDoFuncionario(funcionario.get());
        }

        Credencial credencial = credencialGateway.buscarPorEmail(emailNormalizado)
                .orElseThrow(() -> new CredenciaisInvalidasException("E-mail não encontrado"));
        credencialUseCase.conferirPinEBloquearClienteAposTentativasErradas(credencial, pin);
        return abrirSessaoDoCliente(credencial.clienteId());
    }

    public Sessao abrirSessaoDoCliente(String clienteId) {
        Sessao sessao = new Sessao(gerarTokenAleatorio(), Perfil.CLIENTE, clienteId, clienteId, null,
                Instant.now().plus(duracaoDaSessao));
        sessaoGateway.salvar(sessao);
        return sessao;
    }

    public Sessao buscarSessaoAtiva(String token) {
        if (token == null || token.isBlank()) {
            throw new SessaoInvalidaException();
        }
        return sessaoGateway.buscarPorToken(token)
                .filter(sessao -> sessao.expiraEm().isAfter(Instant.now()))
                .orElseThrow(SessaoInvalidaException::new);
    }

    public void encerrarSessao(String token) {
        if (token != null && !token.isBlank()) {
            sessaoGateway.removerPorToken(token);
        }
    }

    public Sessao transformarClienteEmGerente(String clienteId, String nomeDoGerente) {
        Credencial credencial = credencialUseCase.buscarCredencialDoCliente(clienteId);
        Funcionario gerente = credencialService.criarFuncionario(UUID.randomUUID().toString(), nomeDoGerente,
                credencial.email(), credencial.pinCodificado(), Perfil.GERENTE);
        funcionarioGateway.salvar(gerente);
        credencialUseCase.removerCredencialESessoesDoCliente(clienteId);
        return abrirSessaoDoFuncionario(gerente);
    }

    private Sessao abrirSessaoDoFuncionario(Funcionario funcionario) {
        Sessao sessao = new Sessao(gerarTokenAleatorio(), funcionario.perfil(), funcionario.id(), null,
                funcionario.nome(), Instant.now().plus(duracaoDaSessao));
        sessaoGateway.salvar(sessao);
        return sessao;
    }

    private static String gerarTokenAleatorio() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }
}
