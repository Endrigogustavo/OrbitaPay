package com.orbitapay.pagamentos.domain.model;

import java.time.Instant;

import com.orbitapay.pagamentos.domain.exception.RegraDeNegocioException;

public class Pagamento {

    public static final Dinheiro LIMITE_POR_PAGAMENTO = Dinheiro.de("50000");

    private final String id;
    private final String clienteId;
    private final Dinheiro valor;
    private final MetodoDePagamento metodo;
    private final String provedor;
    private final String referenciaExterna;
    private final InstrucoesDePagamento instrucoes;
    private StatusDoPagamento status;
    private Dinheiro valorPago;
    private final Instant criadoEm;
    private Instant concluidoEm;

    public Pagamento(String id, String clienteId, Dinheiro valor, MetodoDePagamento metodo, String provedor,
            String referenciaExterna, InstrucoesDePagamento instrucoes, StatusDoPagamento status, Dinheiro valorPago,
            Instant criadoEm, Instant concluidoEm) {
        this.id = id;
        this.clienteId = clienteId;
        this.valor = valor;
        this.metodo = metodo;
        this.provedor = provedor;
        this.referenciaExterna = referenciaExterna;
        this.instrucoes = instrucoes;
        this.status = status;
        this.valorPago = valorPago;
        this.criadoEm = criadoEm;
        this.concluidoEm = concluidoEm;
    }

    public static void validarSolicitacao(Dinheiro valor, MetodoDePagamento metodo) {
        if (valor == null || !valor.positivo()) {
            throw new RegraDeNegocioException("Digite um valor maior que zero");
        }
        if (valor.menorQue(metodo.valorMinimo())) {
            throw new RegraDeNegocioException("Valor mínimo para " + metodo.rotulo() + ": " + metodo.valorMinimo().formatado());
        }
        if (valor.maiorQue(LIMITE_POR_PAGAMENTO)) {
            throw new RegraDeNegocioException("Limite por depósito: " + LIMITE_POR_PAGAMENTO.formatado());
        }
    }

    public static Pagamento emitido(String id, String clienteId, Dinheiro valor, MetodoDePagamento metodo,
            String provedor, String referenciaExterna, InstrucoesDePagamento instrucoes, Instant agora) {
        validarSolicitacao(valor, metodo);
        return new Pagamento(id, clienteId, valor, metodo, provedor, referenciaExterna, instrucoes,
                StatusDoPagamento.PENDENTE, null, agora, null);
    }

    public boolean confirmar(Dinheiro valorRecebido, Instant pagoEm) {
        if (!pendente()) {
            return false;
        }
        this.status = StatusDoPagamento.CONFIRMADO;
        this.valorPago = valorRecebido == null || !valorRecebido.positivo() ? valor : valorRecebido;
        this.concluidoEm = pagoEm;
        return true;
    }

    public boolean expirar(Instant agora) {
        if (!pendente()) {
            return false;
        }
        this.status = StatusDoPagamento.EXPIRADO;
        this.concluidoEm = agora;
        return true;
    }

    public boolean pendente() {
        return status == StatusDoPagamento.PENDENTE;
    }

    public boolean pertenceA(String clienteId) {
        return this.clienteId.equals(clienteId);
    }

    public String id() {
        return id;
    }

    public String clienteId() {
        return clienteId;
    }

    public Dinheiro valor() {
        return valor;
    }

    public MetodoDePagamento metodo() {
        return metodo;
    }

    public String provedor() {
        return provedor;
    }

    public String referenciaExterna() {
        return referenciaExterna;
    }

    public InstrucoesDePagamento instrucoes() {
        return instrucoes;
    }

    public StatusDoPagamento status() {
        return status;
    }

    public Dinheiro valorPago() {
        return valorPago;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    public Instant concluidoEm() {
        return concluidoEm;
    }
}
