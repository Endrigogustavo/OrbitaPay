package com.orbitapay.clientes.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.orbitapay.clientes.entity.Conta;
import com.orbitapay.clientes.entity.Lancamento;
import com.orbitapay.clientes.entity.TipoLancamento;
import com.orbitapay.clientes.exception.ContaBloqueadaException;
import com.orbitapay.clientes.exception.RegraDeNegocioException;
import com.orbitapay.clientes.exception.SaldoInsuficienteException;

public class ContaService {

    private static final String AGENCIA = "0001";
    private static final BigDecimal LIMITE_POR_SAQUE = new BigDecimal("5000.00");

    public Conta abrirConta(String contaId, String clienteId, String numero, BigDecimal depositoInicial,
            Instant agora) {
        BigDecimal valorDoDeposito = arredondarParaDuasCasasDecimais(
                depositoInicial == null ? BigDecimal.ZERO : depositoInicial);
        if (valorDoDeposito.signum() < 0) {
            throw new RegraDeNegocioException("Saldo inicial inválido");
        }
        List<Lancamento> lancamentos = new ArrayList<>();
        if (valorDoDeposito.signum() > 0) {
            lancamentos.add(criarLancamento(TipoLancamento.DEPOSITO, valorDoDeposito, "Depósito inicial", null, agora));
        }
        lancamentos.add(criarLancamento(TipoLancamento.ABERTURA, BigDecimal.ZERO, "Conta aberta", null, agora));
        return new Conta(contaId, clienteId, AGENCIA, numero, valorDoDeposito, lancamentos, agora);
    }

    public Conta sacar(Conta conta, boolean titularBloqueado, BigDecimal valor, Instant agora) {
        if (titularBloqueado) {
            throw new ContaBloqueadaException();
        }
        BigDecimal valorDoSaque = validarValorPositivo(valor);
        if (valorDoSaque.compareTo(LIMITE_POR_SAQUE) > 0) {
            throw new RegraDeNegocioException("Limite por saque: " + formatarEmReais(LIMITE_POR_SAQUE));
        }
        garantirSaldoSuficiente(conta, valorDoSaque);
        return adicionarLancamento(conta,
                criarLancamento(TipoLancamento.SAQUE, valorDoSaque.negate(), "Saque · Caixa 24h", null, agora));
    }

    public Conta debitarCompraDeAcoes(Conta conta, String ordemId, BigDecimal valor, String descricao, Instant agora) {
        BigDecimal valorDaCompra = validarValorPositivo(valor);
        garantirSaldoSuficiente(conta, valorDaCompra);
        return adicionarLancamento(conta,
                criarLancamento(TipoLancamento.COMPRA_DE_ACOES, valorDaCompra.negate(), descricao, ordemId, agora));
    }

    public Conta estornarCompraDeAcoes(Conta conta, String ordemId, Instant agora) {
        Lancamento debitoDaCompra = buscarLancamentoPelaReferencia(conta, ordemId);
        if (debitoDaCompra == null) {
            return conta;
        }
        return adicionarLancamento(conta, criarLancamento(TipoLancamento.ESTORNO, debitoDaCompra.valor().negate(),
                "Estorno · " + debitoDaCompra.descricao(), referenciaDoEstorno(ordemId), agora));
    }

    public Conta creditarVendaDeAcoes(Conta conta, String ordemId, BigDecimal valor, String descricao, Instant agora) {
        return adicionarLancamento(conta, criarLancamento(TipoLancamento.VENDA_DE_ACOES, validarValorPositivo(valor),
                descricao, ordemId, agora));
    }

    public Conta creditarDeposito(Conta conta, String pagamentoId, BigDecimal valor, String metodo, Instant agora) {
        return adicionarLancamento(conta, criarLancamento(TipoLancamento.DEPOSITO, validarValorPositivo(valor),
                "Depósito · " + metodo, pagamentoId, agora));
    }

    public Lancamento buscarLancamentoPelaReferencia(Conta conta, String referencia) {
        for (Lancamento lancamento : conta.lancamentos()) {
            if (referencia.equals(lancamento.referencia())) {
                return lancamento;
            }
        }
        return null;
    }

    public String referenciaDoEstorno(String ordemId) {
        return "estorno-" + ordemId;
    }

    private static Conta adicionarLancamento(Conta conta, Lancamento lancamento) {
        List<Lancamento> lancamentosDoMaisNovoParaOMaisAntigo = new ArrayList<>();
        lancamentosDoMaisNovoParaOMaisAntigo.add(lancamento);
        lancamentosDoMaisNovoParaOMaisAntigo.addAll(conta.lancamentos());
        return new Conta(conta.id(), conta.clienteId(), conta.agencia(), conta.numero(),
                conta.saldo().add(lancamento.valor()), lancamentosDoMaisNovoParaOMaisAntigo, conta.abertaEm());
    }

    private static void garantirSaldoSuficiente(Conta conta, BigDecimal valor) {
        if (valor.compareTo(conta.saldo()) > 0) {
            throw new SaldoInsuficienteException(formatarEmReais(conta.saldo()));
        }
    }

    private static BigDecimal validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException("Digite um valor maior que zero");
        }
        return arredondarParaDuasCasasDecimais(valor);
    }

    private static Lancamento criarLancamento(TipoLancamento tipo, BigDecimal valor, String descricao,
            String referencia, Instant agora) {
        return new Lancamento(UUID.randomUUID().toString(), tipo, arredondarParaDuasCasasDecimais(valor), descricao,
                referencia, agora);
    }

    private static BigDecimal arredondarParaDuasCasasDecimais(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    private static String formatarEmReais(BigDecimal valor) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR")));
        return "R$ " + formato.format(valor);
    }
}
