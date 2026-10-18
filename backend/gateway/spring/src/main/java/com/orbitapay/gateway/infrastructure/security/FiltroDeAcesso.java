package com.orbitapay.gateway.infrastructure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import com.orbitapay.gateway.application.dto.Decisao;
import com.orbitapay.gateway.application.usecase.AcessoUseCase;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FiltroDeAcesso extends OncePerRequestFilter {

    public static final String COOKIE_DE_SESSAO = "orbita_sessao";
    public static final String ASSINATURA = "X-Assinatura";

    private static final Logger LOG = LoggerFactory.getLogger(FiltroDeAcesso.class);

    private final AcessoUseCase acessoUseCase;

    public FiltroDeAcesso(AcessoUseCase acessoUseCase) {
        this.acessoUseCase = acessoUseCase;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {
        Decisao decisao = acessoUseCase.decidirSeRequisicaoPodeSeguir(requisicao.getMethod(),
                requisicao.getRequestURI(), lerTokenDoCookieDeSessao(requisicao), requisicao.getHeader(ASSINATURA));
        if (decisao.liberada()) {
            cadeia.doFilter(new RequisicaoComIdentidade(requisicao, decisao.acesso()), resposta);
            return;
        }
        responderRequisicaoNegada(requisicao, resposta, decisao);
    }

    private static void responderRequisicaoNegada(HttpServletRequest requisicao, HttpServletResponse resposta,
            Decisao decisao) throws IOException {
        LOG.info("{} {} -> {} {}", requisicao.getMethod(), requisicao.getRequestURI(), decisao.status(), decisao.codigo());
        resposta.setStatus(decisao.status());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.getWriter().write("{\"codigo\":\"" + decisao.codigo() + "\",\"mensagem\":\"" + decisao.mensagem() + "\"}");
    }

    private static String lerTokenDoCookieDeSessao(HttpServletRequest requisicao) {
        if (requisicao.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : requisicao.getCookies()) {
            if (COOKIE_DE_SESSAO.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
