package com.orbitapay.auth.adapter.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ICookieDeSessao {

    String lerTokenDoCookieDeSessao(HttpServletRequest requisicao);

    void gravarCookieDeSessaoNaResposta(HttpServletResponse resposta, String tokenDeSessao);

    void apagarCookieDeSessaoDaResposta(HttpServletResponse resposta);
}
