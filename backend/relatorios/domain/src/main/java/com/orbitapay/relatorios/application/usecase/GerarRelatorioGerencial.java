package com.orbitapay.relatorios.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orbitapay.relatorios.application.dto.RelatorioGerencial;
import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.PerfilDeCliente;
import com.orbitapay.relatorios.domain.model.Periodo;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

public class GerarRelatorioGerencial {

    private static final int TAMANHO_DOS_RANKINGS = 5;

    private final FatoRepository fatos;
    private final PerfilDeClienteRepository perfis;
    private final Clock relogio;

    public GerarRelatorioGerencial(FatoRepository fatos, PerfilDeClienteRepository perfis, Clock relogio) {
        this.fatos = fatos;
        this.perfis = perfis;
        this.relogio = relogio;
    }

    public RelatorioGerencial executar(LocalDate de, LocalDate ate) {
        Instant agora = Instant.now(relogio);
        Periodo periodo = Periodo.entre(de, ate, LocalDate.ofInstant(agora, Periodo.FUSO));
        List<Fato> doPeriodo = fatos.listarNoPeriodo(periodo);
        List<PerfilDeCliente> base = perfis.listar();
        return new RelatorioGerencial(periodo.de(), periodo.ate(), clientes(base, doPeriodo), negociacao(doPeriodo),
                depositos(doPeriodo), ativosMaisNegociados(doPeriodo), maioresInvestidores(doPeriodo, base),
                movimentoDiario(doPeriodo, periodo), agora);
    }

    private static RelatorioGerencial.Clientes clientes(List<PerfilDeCliente> base, List<Fato> fatos) {
        long ativos = 0;
        long bloqueados = 0;
        for (PerfilDeCliente perfil : base) {
            if (perfil.ativo()) {
                ativos++;
                if (perfil.bloqueado()) {
                    bloqueados++;
                }
            }
        }
        return new RelatorioGerencial.Clientes(ativos, bloqueados, filtrar(fatos, TipoDeFato.CLIENTE_CADASTRADO).size(),
                filtrar(fatos, TipoDeFato.CLIENTE_REMOVIDO).size(), filtrar(fatos, TipoDeFato.CLIENTE_BLOQUEADO).size());
    }

    private static RelatorioGerencial.Negociacao negociacao(List<Fato> fatos) {
        List<Fato> compras = filtrar(fatos, TipoDeFato.COMPRA_EXECUTADA);
        List<Fato> vendas = filtrar(fatos, TipoDeFato.VENDA_EXECUTADA);
        long rejeitadas = filtrar(fatos, TipoDeFato.ORDEM_REJEITADA).size();
        long total = compras.size() + vendas.size() + rejeitadas;
        BigDecimal taxa = BigDecimal.ZERO;
        if (total > 0) {
            taxa = BigDecimal.valueOf(rejeitadas * 100).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_EVEN);
        }
        return new RelatorioGerencial.Negociacao(compras.size(), somar(compras), vendas.size(), somar(vendas),
                rejeitadas, taxa);
    }

    private static RelatorioGerencial.Depositos depositos(List<Fato> fatos) {
        List<Fato> confirmados = filtrar(fatos, TipoDeFato.DEPOSITO_CONFIRMADO);
        BigDecimal volume = somar(confirmados);
        BigDecimal ticketMedio = BigDecimal.ZERO;
        if (!confirmados.isEmpty()) {
            ticketMedio = volume.divide(BigDecimal.valueOf(confirmados.size()), 2, RoundingMode.HALF_EVEN);
        }

        Map<String, List<Fato>> porMetodo = new HashMap<>();
        for (Fato fato : confirmados) {
            String metodo = fato.detalhe() == null ? "?" : fato.detalhe();
            adicionarAoGrupo(porMetodo, metodo, fato);
        }
        List<RelatorioGerencial.PorMetodo> metodos = new ArrayList<>();
        for (Map.Entry<String, List<Fato>> grupo : porMetodo.entrySet()) {
            metodos.add(new RelatorioGerencial.PorMetodo(grupo.getKey(), grupo.getValue().size(),
                    somar(grupo.getValue())));
        }
        metodos.sort((a, b) -> b.volume().compareTo(a.volume()));

        return new RelatorioGerencial.Depositos(confirmados.size(), volume, ticketMedio,
                filtrar(fatos, TipoDeFato.DEPOSITO_EXPIRADO).size(), metodos);
    }

    private static List<RelatorioGerencial.AtivoNegociado> ativosMaisNegociados(List<Fato> fatos) {
        Map<String, List<Fato>> porTicker = new HashMap<>();
        for (Fato fato : fatos) {
            if (fato.tipo().negociacao()) {
                adicionarAoGrupo(porTicker, fato.ticker(), fato);
            }
        }
        List<RelatorioGerencial.AtivoNegociado> ranking = new ArrayList<>();
        for (Map.Entry<String, List<Fato>> grupo : porTicker.entrySet()) {
            long quantidade = 0;
            for (Fato fato : grupo.getValue()) {
                quantidade += fato.quantidade();
            }
            ranking.add(new RelatorioGerencial.AtivoNegociado(grupo.getKey(), grupo.getValue().size(), quantidade,
                    somar(grupo.getValue())));
        }
        ranking.sort((a, b) -> b.volume().compareTo(a.volume()));
        return ranking.subList(0, Math.min(TAMANHO_DOS_RANKINGS, ranking.size()));
    }

    private static List<RelatorioGerencial.Investidor> maioresInvestidores(List<Fato> fatos,
            List<PerfilDeCliente> base) {
        Map<String, List<Fato>> porCliente = new HashMap<>();
        for (Fato fato : fatos) {
            if (fato.tipo().negociacao()) {
                adicionarAoGrupo(porCliente, fato.clienteId(), fato);
            }
        }
        List<RelatorioGerencial.Investidor> ranking = new ArrayList<>();
        for (Map.Entry<String, List<Fato>> grupo : porCliente.entrySet()) {
            ranking.add(new RelatorioGerencial.Investidor(grupo.getKey(), nome(grupo.getKey(), base),
                    grupo.getValue().size(), somar(grupo.getValue())));
        }
        ranking.sort((a, b) -> b.volume().compareTo(a.volume()));
        return ranking.subList(0, Math.min(TAMANHO_DOS_RANKINGS, ranking.size()));
    }

    private static List<RelatorioGerencial.Dia> movimentoDiario(List<Fato> fatos, Periodo periodo) {
        List<RelatorioGerencial.Dia> dias = new ArrayList<>();
        for (LocalDate dia = periodo.de(); !dia.isAfter(periodo.ate()); dia = dia.plusDays(1)) {
            List<Fato> doDia = new ArrayList<>();
            for (Fato fato : fatos) {
                if (periodo.diaDe(fato.ocorridoEm()).equals(dia)) {
                    doDia.add(fato);
                }
            }
            dias.add(new RelatorioGerencial.Dia(dia, somar(filtrar(doDia, TipoDeFato.COMPRA_EXECUTADA)),
                    somar(filtrar(doDia, TipoDeFato.VENDA_EXECUTADA)),
                    somar(filtrar(doDia, TipoDeFato.DEPOSITO_CONFIRMADO))));
        }
        return dias;
    }

    private static String nome(String clienteId, List<PerfilDeCliente> base) {
        for (PerfilDeCliente perfil : base) {
            if (perfil.clienteId().equals(clienteId) && perfil.nome() != null) {
                return perfil.nome();
            }
        }
        return "Cliente encerrado";
    }

    private static void adicionarAoGrupo(Map<String, List<Fato>> grupos, String chave, Fato fato) {
        if (!grupos.containsKey(chave)) {
            grupos.put(chave, new ArrayList<>());
        }
        grupos.get(chave).add(fato);
    }

    private static List<Fato> filtrar(List<Fato> fatos, TipoDeFato tipo) {
        List<Fato> filtrados = new ArrayList<>();
        for (Fato fato : fatos) {
            if (fato.tipo() == tipo) {
                filtrados.add(fato);
            }
        }
        return filtrados;
    }

    private static BigDecimal somar(List<Fato> fatos) {
        BigDecimal total = BigDecimal.ZERO;
        for (Fato fato : fatos) {
            total = total.add(fato.valor());
        }
        return total.setScale(2, RoundingMode.HALF_EVEN);
    }
}
