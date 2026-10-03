package com.orbitapay.ativos.application.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.domain.exception.AtivoNaoEncontradoException;
import com.orbitapay.ativos.domain.model.Ativo;
import com.orbitapay.ativos.domain.model.Bolsa;
import com.orbitapay.ativos.domain.model.Ticker;
import com.orbitapay.ativos.domain.repository.AtivoRepository;
import com.orbitapay.ativos.domain.repository.BolsaRepository;

public class ConsultarAtivos {

    private final AtivoRepository ativos;
    private final BolsaRepository bolsas;

    public ConsultarAtivos(AtivoRepository ativos, BolsaRepository bolsas) {
        this.ativos = ativos;
        this.bolsas = bolsas;
    }

    public List<AtivoCotado> listar() {
        Map<String, Bolsa> bolsasPorCodigo = new HashMap<>();
        for (Bolsa bolsa : bolsas.listar()) {
            bolsasPorCodigo.put(bolsa.codigo(), bolsa);
        }
        List<AtivoCotado> resultado = new ArrayList<>();
        for (Ativo ativo : ativos.listar()) {
            Bolsa bolsa = bolsasPorCodigo.get(ativo.bolsa());
            if (bolsa != null) {
                resultado.add(new AtivoCotado(ativo, bolsa));
            }
        }
        return resultado;
    }

    public AtivoCotado buscar(String ticker) {
        AtivoCotado ativo = procurar(ticker);
        if (ativo == null) {
            throw new AtivoNaoEncontradoException(ticker);
        }
        return ativo;
    }

    public AtivoCotado procurar(String ticker) {
        Optional<Ativo> ativo = ativos.buscar(new Ticker(ticker));
        if (ativo.isEmpty()) {
            return null;
        }
        Optional<Bolsa> bolsa = bolsas.buscar(ativo.get().bolsa());
        if (bolsa.isEmpty()) {
            return null;
        }
        return new AtivoCotado(ativo.get(), bolsa.get());
    }

    public List<Bolsa> bolsas() {
        return bolsas.listar();
    }
}
