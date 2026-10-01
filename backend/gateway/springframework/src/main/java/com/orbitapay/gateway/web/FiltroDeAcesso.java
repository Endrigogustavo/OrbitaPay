package com.orbitapay.gateway.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.orbitapay.gateway.application.ControleDeAcesso;
import com.orbitapay.gateway.domain.Decisao;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FiltroDeAcesso extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(FiltroDeAcesso.class);
    private static final String PREFIXO_BEARER = "Bearer ";

    private final ControleDeAcesso controleDeAcesso;

    public FiltroDeAcesso(ControleDeAcesso controleDeAcesso) {
        this.controleDeAcesso = controleDeAcesso;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        String caminho = requisicao.getRequestURI();
        Decisao decisao = controleDeAcesso.decidir(requisicao.getMethod(), caminho, tokenDeSessao(requisicao),
                requisicao.getHeader(CabecalhosInternos.ASSINATURA));
        switch (decisao) {
            case Decisao.Liberado liberado -> {
                LOG.info("{} {} -> liberado perfil={} cliente={}", requisicao.getMethod(), caminho,
                        liberado.perfil().orElse(null), liberado.clienteId().orElse(null));
                cadeia.doFilter(new RequisicaoComIdentidade(requisicao, liberado), resposta);
            }
            case Decisao.Recusado recusado -> {
                LOG.info("{} {} -> recusado {} {}", requisicao.getMethod(), caminho, recusado.status(),
                        recusado.codigo());
                responderErro(resposta, recusado);
            }
        }
    }

    private static String tokenDeSessao(HttpServletRequest requisicao) {
        String autorizacao = requisicao.getHeader("Authorization");
        return autorizacao != null && autorizacao.startsWith(PREFIXO_BEARER)
                ? autorizacao.substring(PREFIXO_BEARER.length())
                : null;
    }

    private static void responderErro(HttpServletResponse resposta, Decisao.Recusado recusado) throws IOException {
        resposta.setStatus(recusado.status());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.getWriter().write("{\"codigo\":\"" + recusado.codigo() + "\",\"mensagem\":\"" + recusado.mensagem()
                + "\"}");
    }
}
