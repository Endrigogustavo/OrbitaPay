package com.orbitapay.negociacao.application.port.in;

import java.math.BigDecimal;
import java.util.Map;

import com.orbitapay.negociacao.application.dto.AtivoDoCatalogo;

public interface SincronizarAtivosUseCase {

    void registrar(AtivoDoCatalogo ativo);

    void retirar(String ticker);

    void atualizarCotacoes(Map<String, BigDecimal> cotacoes);
}
