package com.orbitapay.clientes.service;

import java.time.Instant;
import java.util.Locale;

import com.orbitapay.clientes.entity.Cliente;
import com.orbitapay.clientes.entity.MotivoBloqueio;
import com.orbitapay.clientes.exception.RegraDeNegocioException;

public class ClienteService {

    public Cliente criarNovoCliente(String clienteId, String nome, String email, String cpf, Instant agora) {
        return new Cliente(clienteId, validarNomeCompleto(nome), validarENormalizarEmail(email), validarEFormatarCpf(cpf),
                false, null, agora);
    }

    public Cliente atualizarDadosPessoais(Cliente cliente, String nome, String email, String cpfOuNuloParaManter) {
        String cpf = cpfOuNuloParaManter == null ? cliente.cpf() : validarEFormatarCpf(cpfOuNuloParaManter);
        return new Cliente(cliente.id(), validarNomeCompleto(nome), validarENormalizarEmail(email), cpf,
                cliente.bloqueado(), cliente.motivoBloqueio(), cliente.clienteDesde());
    }

    public Cliente bloquear(Cliente cliente, MotivoBloqueio motivo) {
        if (cliente.bloqueado()) {
            throw new RegraDeNegocioException("A conta já está bloqueada");
        }
        return alterarSituacao(cliente, true, motivo);
    }

    public Cliente desbloquearPeloProprioCliente(Cliente cliente) {
        garantirQueEstaBloqueado(cliente);
        if (cliente.motivoBloqueio() != MotivoBloqueio.CLIENTE) {
            throw new RegraDeNegocioException("Somente o gerente pode desbloquear esta conta");
        }
        return alterarSituacao(cliente, false, null);
    }

    public Cliente desbloquearPeloGerente(Cliente cliente) {
        garantirQueEstaBloqueado(cliente);
        return alterarSituacao(cliente, false, null);
    }

    private static void garantirQueEstaBloqueado(Cliente cliente) {
        if (!cliente.bloqueado()) {
            throw new RegraDeNegocioException("A conta não está bloqueada");
        }
    }

    private static Cliente alterarSituacao(Cliente cliente, boolean bloqueado, MotivoBloqueio motivo) {
        return new Cliente(cliente.id(), cliente.nome(), cliente.email(), cliente.cpf(), bloqueado, motivo,
                cliente.clienteDesde());
    }

    private static String validarNomeCompleto(String nome) {
        String nomeSemEspacosExtras = nome == null ? "" : nome.trim().replaceAll("\\s+", " ");
        if (nomeSemEspacosExtras.split(" ").length < 2) {
            throw new RegraDeNegocioException("Informe nome e sobrenome");
        }
        return nomeSemEspacosExtras;
    }

    private static String validarENormalizarEmail(String email) {
        if (email == null || !email.trim().matches("^\\S+@\\S+\\.\\S+$")) {
            throw new RegraDeNegocioException("E-mail inválido");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String validarEFormatarCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new RegraDeNegocioException("CPF precisa de 11 dígitos");
        }
        return digitos.substring(0, 3) + "." + digitos.substring(3, 6) + "." + digitos.substring(6, 9) + "-"
                + digitos.substring(9);
    }
}
