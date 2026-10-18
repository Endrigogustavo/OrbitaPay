package com.orbitapay.auth.infrastructure.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import com.orbitapay.auth.adapter.interfaces.ICookieDeSessao;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieDeSessaoHttpOnly implements ICookieDeSessao {

    private static final String NOME_DO_COOKIE_DE_SESSAO = "orbita_sessao";

    private final Duration duracaoDaSessao;
    private final boolean enviarCookieSomenteEmHttps;

    public CookieDeSessaoHttpOnly(@Value("${orbita.sessao.duracao}") Duration duracaoDaSessao,
            @Value("${orbita.sessao.cookie-seguro}") boolean enviarCookieSomenteEmHttps) {
        this.duracaoDaSessao = duracaoDaSessao;
        this.enviarCookieSomenteEmHttps = enviarCookieSomenteEmHttps;
    }

    @Override
    public String lerTokenDoCookieDeSessao(HttpServletRequest requisicao) {
        if (requisicao.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : requisicao.getCookies()) {
            if (NOME_DO_COOKIE_DE_SESSAO.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public void gravarCookieDeSessaoNaResposta(HttpServletResponse resposta, String tokenDeSessao) {
        resposta.addHeader(HttpHeaders.SET_COOKIE, montarCookieDeSessaoHttpOnly(tokenDeSessao, duracaoDaSessao));
    }

    @Override
    public void apagarCookieDeSessaoDaResposta(HttpServletResponse resposta) {
        resposta.addHeader(HttpHeaders.SET_COOKIE, montarCookieDeSessaoHttpOnly("", Duration.ZERO));
    }

    private String montarCookieDeSessaoHttpOnly(String valorDoCookie, Duration validadeDoCookie) {
        return ResponseCookie.from(NOME_DO_COOKIE_DE_SESSAO, valorDoCookie)
                .httpOnly(true)
                .secure(enviarCookieSomenteEmHttps)
                .sameSite("Lax")
                .path("/")
                .maxAge(validadeDoCookie)
                .build()
                .toString();
    }
}
