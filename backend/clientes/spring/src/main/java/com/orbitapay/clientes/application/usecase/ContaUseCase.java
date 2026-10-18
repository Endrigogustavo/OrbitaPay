package com.orbitapay.clientes.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.UnaryOperator;

import com.orbitapay.clientes.application.exception.NaoEncontradoException;
import com.orbitapay.clientes.application.gateway.IClienteGateway;
import com.orbitapay.clientes.application.gateway.IContaGateway;
import com.orbitapay.clientes.application.gateway.ITransacaoGateway;
import com.orbitapay.clientes.entity.Cliente;
import com.orbitapay.clientes.entity.Comprovante;
import com.orbitapay.clientes.entity.Conta;
import com.orbitapay.clientes.entity.Lancamento;
import com.orbitapay.clientes.service.ContaService;

public class ContaUseCase {

    private final ContaService contaService;
    private final IContaGateway contaGateway;
    private final IClienteGateway clienteGateway;
    private final ITransacaoGateway transacaoGateway;

    public ContaUseCase(ContaService contaService, IContaGateway contaGateway, IClienteGateway clienteGateway,
            ITransacaoGateway transacaoGateway) {
        this.contaService = contaService;
        this.contaGateway = contaGateway;
        this.clienteGateway = clienteGateway;
        this.transacaoGateway = transacaoGateway;
    }

    public Conta abrirContaDoCliente(String clienteId, BigDecimal depositoInicial) {
        Conta contaExistente = contaGateway.buscarPorCliente(clienteId).orElse(null);
        if (contaExistente != null) {
            return contaExistente;
        }
        Conta novaConta = contaService.abrirConta(UUID.randomUUID().toString(), clienteId, gerarNumeroDaConta(),
                depositoInicial, Instant.now());
        contaGateway.salvar(novaConta);
        return novaConta;
    }

    public Conta buscarContaDoCliente(String clienteId) {
        return contaGateway.buscarPorCliente(clienteId)
                .orElseThrow(() -> new NaoEncontradoException("Conta não encontrada para o cliente " + clienteId));
    }

    public List<Conta> listarContas() {
        return contaGateway.listarTodas();
    }

    public void encerrarContaDoCliente(String clienteId) {
        contaGateway.removerPorCliente(clienteId);
    }

    public Comprovante sacarDinheiroDaConta(String clienteId, BigDecimal valor) {
        Conta contaDepoisDoSaque = transacaoGateway.executarEmTransacaoERetornar(() -> {
            boolean titularBloqueado = clienteGateway.buscarPorId(clienteId).map(Cliente::bloqueado).orElse(false);
            Conta conta = contaService.sacar(buscarContaDoCliente(clienteId), titularBloqueado, valor, Instant.now());
            contaGateway.salvar(conta);
            return conta;
        });
        Lancamento saque = contaDepoisDoSaque.lancamentos().getFirst();
        String protocolo = saque.id().replace("-", "").substring(0, 8).toUpperCase(Locale.ROOT);
        return new Comprovante(saque, contaDepoisDoSaque.saldo(), protocolo);
    }

    public void debitarCompraDeAcoes(String clienteId, String ordemId, BigDecimal valor, String descricao) {
        movimentarContaUmaUnicaVez(clienteId, ordemId,
                conta -> contaService.debitarCompraDeAcoes(conta, ordemId, valor, descricao, Instant.now()));
    }

    public void estornarCompraDeAcoes(String clienteId, String ordemId) {
        movimentarContaUmaUnicaVez(clienteId, contaService.referenciaDoEstorno(ordemId),
                conta -> contaService.estornarCompraDeAcoes(conta, ordemId, Instant.now()));
    }

    public void creditarVendaDeAcoes(String clienteId, String ordemId, BigDecimal valor, String descricao) {
        movimentarContaUmaUnicaVez(clienteId, ordemId,
                conta -> contaService.creditarVendaDeAcoes(conta, ordemId, valor, descricao, Instant.now()));
    }

    public void creditarDepositoConfirmado(String clienteId, String pagamentoId, BigDecimal valor, String metodo) {
        movimentarContaUmaUnicaVez(clienteId, pagamentoId,
                conta -> contaService.creditarDeposito(conta, pagamentoId, valor, metodo, Instant.now()));
    }

    private void movimentarContaUmaUnicaVez(String clienteId, String referenciaDaOperacao,
            UnaryOperator<Conta> aplicarRegraDeNegocio) {
        transacaoGateway.executarEmTransacao(() -> {
            Conta conta = buscarContaDoCliente(clienteId);
            boolean operacaoJaFoiRegistrada = contaService.buscarLancamentoPelaReferencia(conta, referenciaDaOperacao) != null;
            if (!operacaoJaFoiRegistrada) {
                contaGateway.salvar(aplicarRegraDeNegocio.apply(conta));
            }
        });
    }

    private static String gerarNumeroDaConta() {
        ThreadLocalRandom aleatorio = ThreadLocalRandom.current();
        return String.format("%05d-%d", aleatorio.nextInt(100000), aleatorio.nextInt(10));
    }
}
