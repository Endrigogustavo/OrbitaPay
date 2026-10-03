package com.orbitapay.gateway.web;

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

    public RequisicaoComIdentidade(HttpServletRequest requisicao, Decisao decisao) {
        super(requisicao);
        if (decisao.clienteId() != null) {
            injetados.put(CabecalhosInternos.CLIENTE_ID, decisao.clienteId());
        }
        if (decisao.perfil() != null) {
            injetados.put(CabecalhosInternos.PERFIL, decisao.perfil().name());
        }
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
        List<String> nomes = new ArrayList<>();
        for (String nome : Collections.list(super.getHeaderNames())) {
            if (!controlado(nome)) {
                nomes.add(nome);
            }
        }
        nomes.addAll(injetados.keySet());
        return Collections.enumeration(nomes);
    }

    private String injetado(String nome) {
        for (Map.Entry<String, String> cabecalho : injetados.entrySet()) {
            if (cabecalho.getKey().equalsIgnoreCase(nome)) {
                return cabecalho.getValue();
            }
        }
        return null;
    }

    private static boolean controlado(String nome) {
        return CabecalhosInternos.CONTROLADOS_PELO_GATEWAY.contains(nome.toLowerCase(Locale.ROOT));
    }
}
