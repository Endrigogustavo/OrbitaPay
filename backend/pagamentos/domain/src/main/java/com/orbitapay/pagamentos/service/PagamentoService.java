package com.orbitapay.pagamentos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;

import com.orbitapay.pagamentos.entity.InstrucoesDePagamento;
import com.orbitapay.pagamentos.entity.MetodoDePagamento;
import com.orbitapay.pagamentos.entity.Pagamento;
import com.orbitapay.pagamentos.entity.StatusDoPagamento;
import com.orbitapay.pagamentos.exception.RegraDeNegocioException;

public class PagamentoService {

    private static final BigDecimal LIMITE_POR_DEPOSITO = new BigDecimal("50000.00");

    public MetodoDePagamento converterMetodoDePagamento(String metodoInformado) {
        String metodoNormalizado = metodoInformado == null ? "" : metodoInformado.trim().toUpperCase(Locale.ROOT);
        for (MetodoDePagamento metodo : MetodoDePagamento.values()) {
            if (metodo.name().equals(metodoNormalizado)) {
                return metodo;
            }
        }
        throw new RegraDeNegocioException("Método de pagamento inválido: " + metodoInformado);
    }

    public BigDecimal validarValorDoDeposito(BigDecimal valor, MetodoDePagamento metodo) {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraDeNegocioException("Digite um valor maior que zero");
        }
        BigDecimal valorArredondado = valor.setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal valorMinimo = valorMinimoDoMetodo(metodo);
        if (valorArredondado.compareTo(valorMinimo) < 0) {
            throw new RegraDeNegocioException("Valor mínimo para " + metodo.name() + ": R$ " + valorMinimo);
        }
        if (valorArredondado.compareTo(LIMITE_POR_DEPOSITO) > 0) {
            throw new RegraDeNegocioException("Limite por depósito: R$ 50.000,00");
        }
        return valorArredondado;
    }

    public Pagamento criarPagamentoPendente(String pagamentoId, String clienteId, BigDecimal valor,
            MetodoDePagamento metodo, InstrucoesDePagamento instrucoes, Instant agora) {
        return new Pagamento(pagamentoId, clienteId, valor, metodo, instrucoes, StatusDoPagamento.PENDENTE, null, agora,
                null);
    }

    public Pagamento confirmarPagamento(Pagamento pagamento, Instant agora) {
        return concluir(pagamento, StatusDoPagamento.CONFIRMADO, pagamento.valor(), agora);
    }

    public Pagamento expirarPagamento(Pagamento pagamento, Instant agora) {
        return concluir(pagamento, StatusDoPagamento.EXPIRADO, null, agora);
    }

    private static Pagamento concluir(Pagamento pagamento, StatusDoPagamento novoStatus, BigDecimal valorPago,
            Instant agora) {
        if (pagamento.status() != StatusDoPagamento.PENDENTE) {
            throw new RegraDeNegocioException("O pagamento " + pagamento.id() + " já foi concluído");
        }
        return new Pagamento(pagamento.id(), pagamento.clienteId(), pagamento.valor(), pagamento.metodo(),
                pagamento.instrucoes(), novoStatus, valorPago, pagamento.criadoEm(), agora);
    }

    private static BigDecimal valorMinimoDoMetodo(MetodoDePagamento metodo) {
        return switch (metodo) {
            case PIX -> new BigDecimal("0.01");
            case TED -> new BigDecimal("1.00");
            case BOLETO -> new BigDecimal("10.00");
        };
    }
}
