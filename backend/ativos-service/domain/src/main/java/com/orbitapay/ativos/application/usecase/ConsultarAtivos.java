package com.orbitapay.ativos.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.orbitapay.ativos.application.dto.AtivoCotado;
import com.orbitapay.ativos.application.port.in.ConsultarAtivosUseCase;
import com.orbitapay.ativos.application.port.out.AtivoRepository;
import com.orbitapay.ativos.application.port.out.BolsaRepository;
import com.orbitapay.ativos.domain.exception.AtivoNaoEncontradoException;
import com.orbitapay.ativos.domain.model.Bolsa;
import com.orbitapay.ativos.domain.model.Ticker;

public class ConsultarAtivos implements ConsultarAtivosUseCase {

    private final AtivoRepository ativos;
    private final BolsaRepository bolsas;

    public ConsultarAtivos(AtivoRepository ativos, BolsaRepository bolsas) {
        this.ativos = ativos;
        this.bolsas = bolsas;
    }

    @Override
    public List<AtivoCotado> listar() {
        Map<String, Bolsa> porCodigo = bolsas.listar().stream()
                .collect(Collectors.toMap(Bolsa::codigo, Function.identity()));
        return ativos.listar().stream()
                .filter(ativo -> porCodigo.containsKey(ativo.bolsa()))
                .map(ativo -> new AtivoCotado(ativo, porCodigo.get(ativo.bolsa())))
                .toList();
    }

    @Override
    public AtivoCotado buscar(String ticker) {
        return procurar(ticker).orElseThrow(() -> new AtivoNaoEncontradoException(ticker));
    }

    @Override
    public Optional<AtivoCotado> procurar(String ticker) {
        return ativos.buscar(new Ticker(ticker))
                .flatMap(ativo -> bolsas.buscar(ativo.bolsa()).map(bolsa -> new AtivoCotado(ativo, bolsa)));
    }

    @Override
    public List<Bolsa> bolsas() {
        return bolsas.listar();
    }
}
