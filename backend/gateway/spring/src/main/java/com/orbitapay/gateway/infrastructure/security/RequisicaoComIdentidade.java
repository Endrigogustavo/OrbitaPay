package com.orbitapay.gateway.infrastructure.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.orbitapay.gateway.entity.Acesso;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class RequisicaoComIdentidade extends HttpServletRequestWrapper {

    private static final String CLIENTE_ID = "X-Cliente-Id";
    private static final String PERFIL = "X-Perfil";
    private static final Set<String> CONTROLADOS_PELO_GATEWAY = Set.of("x-cliente-id", "x-perfil", "x-assinatura");

    private final Map<String, String> cabecalhosInjetados = new LinkedHashMap<>();

    public RequisicaoComIdentidade(HttpServletRequest requisicao, Acesso acesso) {
        super(requisicao);
        if (acesso != null) {
            cabecalhosInjetados.put(PERFIL, acesso.perfil().name());
            if (acesso.clienteId() != null) {
                cabecalhosInjetados.put(CLIENTE_ID, acesso.clienteId());
            }
        }
    }

    @Override
    public String getHeader(String nome) {
        String valorInjetado = buscarValorInjetadoPeloGateway(nome);
        if (valorInjetado != null) {
            return valorInjetado;
        }
        return cabecalhoControladoPeloGateway(nome) ? null : super.getHeader(nome);
    }

    @Override
    public Enumeration<String> getHeaders(String nome) {
        String valorInjetado = buscarValorInjetadoPeloGateway(nome);
        if (valorInjetado != null) {
            return Collections.enumeration(List.of(valorInjetado));
        }
        return cabecalhoControladoPeloGateway(nome) ? Collections.emptyEnumeration() : super.getHeaders(nome);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> nomes = new ArrayList<>();
        for (String nome : Collections.list(super.getHeaderNames())) {
            if (!cabecalhoControladoPeloGateway(nome)) {
                nomes.add(nome);
            }
        }
        nomes.addAll(cabecalhosInjetados.keySet());
        return Collections.enumeration(nomes);
    }

    private String buscarValorInjetadoPeloGateway(String nome) {
        for (Map.Entry<String, String> cabecalho : cabecalhosInjetados.entrySet()) {
            if (cabecalho.getKey().equalsIgnoreCase(nome)) {
                return cabecalho.getValue();
            }
        }
        return null;
    }

    private static boolean cabecalhoControladoPeloGateway(String nome) {
        return CONTROLADOS_PELO_GATEWAY.contains(nome.toLowerCase(Locale.ROOT));
    }
}
