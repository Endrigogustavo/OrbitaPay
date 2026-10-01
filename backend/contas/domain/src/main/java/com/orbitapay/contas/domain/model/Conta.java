package com.orbitapay.contas.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.orbitapay.contas.domain.exception.ContaBloqueadaException;
import com.orbitapay.contas.domain.exception.RegraDeNegocioException;
import com.orbitapay.contas.domain.exception.SaldoInsuficienteException;

public class Conta {

    public static final String AGENCIA = "0001";
    public static final Dinheiro LIMITE_POR_SAQUE = Dinheiro.de("5000");

    private final String id;
    private final String clienteId;
    private String nomeTitular;
    private boolean titularBloqueado;
    private final String numero;
    private Dinheiro saldo;
    private final List<Lancamento> lancamentos;
    private final Instant abertaEm;

    private Conta(String id, String clienteId, String nomeTitular, boolean titularBloqueado, String numero,
            Dinheiro saldo, List<Lancamento> lancamentos, Instant abertaEm) {
        this.id = Objects.requireNonNull(id);
        this.clienteId = Objects.requireNonNull(clienteId);
        this.nomeTitular = Objects.requireNonNull(nomeTitular);
        this.titularBloqueado = titularBloqueado;
        this.numero = Objects.requireNonNull(numero);
        this.saldo = Objects.requireNonNull(saldo);
        this.lancamentos = new ArrayList<>(lancamentos);
        this.abertaEm = Objects.requireNonNull(abertaEm);
    }

    public static Conta abrir(String id, String clienteId, String nomeTitular, String numero, Dinheiro depositoInicial,
            Instant agora) {
        Conta conta = new Conta(id, clienteId, nomeTitular, false, numero, Dinheiro.ZERO, List.of(), agora);
        conta.registrar(TipoLancamento.ABERTURA, Dinheiro.ZERO, "Conta aberta", null, agora);
        if (depositoInicial.positivo()) {
            conta.saldo = depositoInicial;
            conta.registrar(TipoLancamento.DEPOSITO, depositoInicial, "Depósito inicial", null, agora);
        }
        return conta;
    }

    public static Conta reconstituir(String id, String clienteId, String nomeTitular, boolean titularBloqueado,
            String numero, Dinheiro saldo, List<Lancamento> lancamentos, Instant abertaEm) {
        return new Conta(id, clienteId, nomeTitular, titularBloqueado, numero, saldo, lancamentos, abertaEm);
    }

    /**
     * Credita um depósito já pago, confirmado pelo contexto de Pagamentos. Os limites por depósito foram aplicados
     * quando a cobrança foi emitida; aqui o dinheiro já entrou, então só a idempotência importa.
     */
    public Lancamento creditarDeposito(String pagamentoId, Dinheiro valor, String metodo, Instant agora) {
        Optional<Lancamento> existente = lancamentoReferenteA(pagamentoId);
        if (existente.isPresent()) {
            return existente.get();
        }
        exigirPositivo(valor);
        saldo = saldo.somar(valor);
        return registrar(TipoLancamento.DEPOSITO, valor, "Depósito · " + metodo, pagamentoId, agora);
    }

    public Lancamento sacar(Dinheiro valor, Instant agora) {
        exigirTitularLiberado();
        exigirPositivo(valor);
        exigirSaldo(valor);
        if (valor.maiorQue(LIMITE_POR_SAQUE)) {
            throw new RegraDeNegocioException("Limite por saque: " + LIMITE_POR_SAQUE.formatado());
        }
        saldo = saldo.subtrair(valor);
        return registrar(TipoLancamento.SAQUE, valor.negativo(), "Saque · Caixa 24h", null, agora);
    }

    public Lancamento debitarCompraDeAcoes(String ordemId, Dinheiro valor, String descricao, Instant agora) {
        Optional<Lancamento> existente = lancamentoReferenteA(ordemId);
        if (existente.isPresent()) {
            return existente.get();
        }
        exigirTitularLiberado();
        exigirPositivo(valor);
        exigirSaldo(valor);
        saldo = saldo.subtrair(valor);
        return registrar(TipoLancamento.COMPRA_DE_ACOES, valor.negativo(), descricao, ordemId, agora);
    }

    public Lancamento creditarVendaDeAcoes(String ordemId, Dinheiro valor, String descricao, Instant agora) {
        Optional<Lancamento> existente = lancamentoReferenteA(ordemId);
        if (existente.isPresent()) {
            return existente.get();
        }
        exigirPositivo(valor);
        saldo = saldo.somar(valor);
        return registrar(TipoLancamento.VENDA_DE_ACOES, valor, descricao, ordemId, agora);
    }

    public void atualizarTitular(String nome) {
        this.nomeTitular = Objects.requireNonNull(nome);
    }

    public void alterarSituacaoDoTitular(boolean bloqueado) {
        this.titularBloqueado = bloqueado;
    }

    private Optional<Lancamento> lancamentoReferenteA(String referencia) {
        return lancamentos.stream().filter(l -> l.referenteA(referencia)).findFirst();
    }

    private void exigirTitularLiberado() {
        if (titularBloqueado) {
            throw new ContaBloqueadaException();
        }
    }

    private static void exigirPositivo(Dinheiro valor) {
        if (valor == null || !valor.positivo()) {
            throw new RegraDeNegocioException("Digite um valor maior que zero");
        }
    }

    private void exigirSaldo(Dinheiro valor) {
        if (valor.maiorQue(saldo)) {
            throw new SaldoInsuficienteException(saldo);
        }
    }

    private Lancamento registrar(TipoLancamento tipo, Dinheiro valor, String descricao, String referencia,
            Instant agora) {
        Lancamento lancamento = new Lancamento(UUID.randomUUID().toString(), tipo, valor, descricao, referencia, agora);
        lancamentos.addFirst(lancamento);
        return lancamento;
    }

    public String id() {
        return id;
    }

    public String clienteId() {
        return clienteId;
    }

    public String nomeTitular() {
        return nomeTitular;
    }

    public boolean titularBloqueado() {
        return titularBloqueado;
    }

    public String agencia() {
        return AGENCIA;
    }

    public String numero() {
        return numero;
    }

    public Dinheiro saldo() {
        return saldo;
    }

    public List<Lancamento> lancamentos() {
        return List.copyOf(lancamentos);
    }

    public Instant abertaEm() {
        return abertaEm;
    }
}
