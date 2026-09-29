package com.orbitapay.gateway.adapter.in.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.orbitapay.gateway.domain.Decisao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RequisicaoComIdentidade extends HttpServletRequestWrapper {

    private final Map<String, String> injetados = new LinkedHashMap<>();

    public RequisicaoComIdentidade(HttpServletRequest requisicao, Decisao.Liberado liberado) {
        super(requisicao);
        liberado.clienteId().ifPresent(id -> injetados.put(CabecalhosInternos.CLIENTE_ID, id));
        liberado.perfil().ifPresent(perfil -> injetados.put(CabecalhosInternos.PERFIL, perfil.name()));
    }

    @Override
    public String getHeader(String nome) {
        String injetado = injetado(nome);
        if (injetado != null) {
            return injetado;
        }
        return controlado(nome) ? null : super.getHeader(nome);
    }

    @Override
    public Enumeration<String> getHeaders(String nome) {
        String injetado = injetado(nome);
        if (injetado != null) {
            return Collections.enumeration(List.of(injetado));
        }
        return controlado(nome) ? Collections.emptyEnumeration() : super.getHeaders(nome);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> nomes = Collections.list(super.getHeaderNames()).stream().filter(n -> !controlado(n)).toList();
        List<String> todos = new ArrayList<>(nomes);
        todos.addAll(injetados.keySet());
        return Collections.enumeration(todos);
    }

    private String injetado(String nome) {
        return injetados.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(nome)).map(Map.Entry::getValue)
                .findFirst().orElse(null);
    }

    private static boolean controlado(String nome) {
        return CabecalhosInternos.CONTROLADOS_PELO_GATEWAY.contains(nome.toLowerCase(Locale.ROOT));
    }
}
