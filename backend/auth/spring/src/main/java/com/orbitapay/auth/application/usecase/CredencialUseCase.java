package com.orbitapay.auth.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import com.orbitapay.auth.application.exception.NaoEncontradoException;
import com.orbitapay.auth.application.exception.PinIncorretoException;
import com.orbitapay.auth.application.gateway.IClienteGateway;
import com.orbitapay.auth.application.gateway.ICodificadorDePinGateway;
import com.orbitapay.auth.application.gateway.ICredencialGateway;
import com.orbitapay.auth.application.gateway.IFuncionarioGateway;
import com.orbitapay.auth.application.gateway.ISessaoGateway;
import com.orbitapay.auth.entity.Credencial;
import com.orbitapay.auth.entity.Funcionario;
import com.orbitapay.auth.entity.Perfil;
import com.orbitapay.auth.exception.RegraDeNegocioException;
import com.orbitapay.auth.service.CredencialService;

public class CredencialUseCase {

    public static final int LIMITE_DE_TENTATIVAS_DE_PIN = 3;
    private static final Duration PRAZO_PARA_DESFAZER_CADASTRO = Duration.ofMinutes(10);

    private final CredencialService credencialService;
    private final ICredencialGateway credencialGateway;
    private final IFuncionarioGateway funcionarioGateway;
    private final ISessaoGateway sessaoGateway;
    private final IClienteGateway clienteGateway;
    private final ICodificadorDePinGateway codificadorDePinGateway;

    public CredencialUseCase(CredencialService credencialService, ICredencialGateway credencialGateway,
            IFuncionarioGateway funcionarioGateway, ISessaoGateway sessaoGateway, IClienteGateway clienteGateway,
            ICodificadorDePinGateway codificadorDePinGateway) {
        this.credencialService = credencialService;
        this.credencialGateway = credencialGateway;
        this.funcionarioGateway = funcionarioGateway;
        this.sessaoGateway = sessaoGateway;
        this.clienteGateway = clienteGateway;
        this.codificadorDePinGateway = codificadorDePinGateway;
    }

    public Credencial cadastrarCredencialDeNovoCliente(String email, String pin) {
        return cadastrarCredencialDoCliente(UUID.randomUUID().toString(), email, pin);
    }

    public Credencial cadastrarCredencialDoCliente(String clienteId, String email, String pin) {
        String emailNormalizado = credencialService.validarENormalizarEmail(email);
        credencialService.validarFormatoDoPin(pin);
        Credencial credencialExistente = credencialGateway.buscarPorCliente(clienteId).orElse(null);
        if (credencialExistente != null) {
            return credencialExistente;
        }
        garantirQueEmailNaoEstaEmUso(emailNormalizado, clienteId);
        Credencial novaCredencial = new Credencial(clienteId, emailNormalizado,
                codificadorDePinGateway.codificarPin(pin), 0, Instant.now());
        credencialGateway.salvar(novaCredencial);
        return novaCredencial;
    }

    public Funcionario cadastrarFuncionarioDaEquipe(String nome, String email, String pin, Perfil perfil) {
        credencialService.validarFormatoDoPin(pin);
        Funcionario funcionario = credencialService.criarFuncionario(UUID.randomUUID().toString(), nome, email,
                codificadorDePinGateway.codificarPin(pin), perfil);
        garantirQueEmailNaoEstaEmUso(funcionario.email(), null);
        funcionarioGateway.salvar(funcionario);
        return funcionario;
    }

    public Credencial buscarCredencialDoCliente(String clienteId) {
        return credencialGateway.buscarPorCliente(clienteId)
                .orElseThrow(() -> new NaoEncontradoException("Credencial não encontrada: " + clienteId));
    }

    public void desfazerCadastroFeitoHaPouco(String clienteId) {
        Credencial credencial = buscarCredencialDoCliente(clienteId);
        Instant fimDoPrazo = credencial.criadaEm().plus(PRAZO_PARA_DESFAZER_CADASTRO);
        if (Instant.now().isAfter(fimDoPrazo)) {
            throw new RegraDeNegocioException("O cadastro já foi concluído · para sair da Órbita, encerre a conta");
        }
        removerCredencialESessoesDoCliente(clienteId);
    }

    public void removerCredencialESessoesDoCliente(String clienteId) {
        credencialGateway.remover(clienteId);
        sessaoGateway.removerTodasDoSujeito(clienteId);
    }

    public void alterarPinDoCliente(String clienteId, String pinAtual, String novoPin) {
        Credencial credencial = buscarCredencialDoCliente(clienteId);
        boolean pinAtualConfere = pinAtual != null && pinAtual.matches("\\d{4}")
                && codificadorDePinGateway.pinConfereComOCodificado(pinAtual, credencial.pinCodificado());
        if (!pinAtualConfere) {
            throw new RegraDeNegocioException("PIN atual incorreto");
        }
        credencialService.validarTrocaDePin(pinAtual, novoPin);
        credencialGateway.salvar(new Credencial(clienteId, credencial.email(),
                codificadorDePinGateway.codificarPin(novoPin), credencial.tentativasFalhas(), credencial.criadaEm()));
    }

    public void atualizarEmailDeLoginDoCliente(String clienteId, String novoEmail) {
        String emailNormalizado = credencialService.validarENormalizarEmail(novoEmail);
        Credencial credencial = buscarCredencialDoCliente(clienteId);
        if (credencial.email().equals(emailNormalizado)) {
            return;
        }
        garantirQueEmailNaoEstaEmUso(emailNormalizado, clienteId);
        credencialGateway.salvar(new Credencial(clienteId, emailNormalizado, credencial.pinCodificado(),
                credencial.tentativasFalhas(), credencial.criadaEm()));
    }

    public void conferirPinEBloquearClienteAposTentativasErradas(Credencial credencial, String pinInformado) {
        credencialService.validarFormatoDoPin(pinInformado);
        if (codificadorDePinGateway.pinConfereComOCodificado(pinInformado, credencial.pinCodificado())) {
            if (credencial.tentativasFalhas() > 0) {
                salvarTentativasFalhas(credencial, 0);
            }
            return;
        }

        int tentativasFalhas = credencial.tentativasFalhas() + 1;
        salvarTentativasFalhas(credencial, tentativasFalhas);
        if (tentativasFalhas < LIMITE_DE_TENTATIVAS_DE_PIN) {
            throw new PinIncorretoException(LIMITE_DE_TENTATIVAS_DE_PIN - tentativasFalhas, false);
        }

        clienteGateway.bloquearPorPin(credencial.clienteId());
        salvarTentativasFalhas(credencial, 0);
        throw new PinIncorretoException(0, true);
    }

    private void garantirQueEmailNaoEstaEmUso(String email, String clienteId) {
        boolean usadoPorOutroCliente = clienteId == null
                ? credencialGateway.buscarPorEmail(email).isPresent()
                : credencialGateway.emailEmUsoPorOutroCliente(email, clienteId);
        if (usadoPorOutroCliente || funcionarioGateway.existePorEmail(email)) {
            throw new RegraDeNegocioException("Este e-mail já tem conta");
        }
    }

    private void salvarTentativasFalhas(Credencial credencial, int tentativasFalhas) {
        credencialGateway.salvar(new Credencial(credencial.clienteId(), credencial.email(), credencial.pinCodificado(),
                tentativasFalhas, credencial.criadaEm()));
    }
}
