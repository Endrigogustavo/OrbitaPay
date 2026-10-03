package com.orbitapay.carteira.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.orbitapay.carteira.application.dto.CarteiraValorizada.PosicaoValorizada;
import com.orbitapay.carteira.application.dto.CarteiraValorizada;
import com.orbitapay.carteira.application.service.CatalogoDeAtivos;
import com.orbitapay.carteira.domain.model.AtivoCotado;
import com.orbitapay.carteira.domain.model.Carteira;
import com.orbitapay.carteira.domain.model.Posicao;
import com.orbitapay.carteira.domain.repository.AtivoCotadoRepository;
import com.orbitapay.carteira.domain.repository.CarteiraRepository;

public class ConsultarCarteira {

    private final CarteiraRepository carteiras;
    private final AtivoCotadoRepository ativos;
    private final CatalogoDeAtivos catalogo;

    public ConsultarCarteira(CarteiraRepository carteiras, AtivoCotadoRepository ativos, CatalogoDeAtivos catalogo) {
        this.carteiras = carteiras;
        this.ativos = ativos;
        this.catalogo = catalogo;
    }

    public CarteiraValorizada doCliente(String clienteId) {
        Optional<Carteira> carteira = carteiras.buscarPorCliente(clienteId);
        List<Posicao> posicoes = carteira.isPresent() ? carteira.get().posicoes() : List.of();
        List<String> tickers = new ArrayList<>();
        for (Posicao posicao : posicoes) {
            tickers.add(posicao.ticker());
        }
        Map<String, AtivoCotado> cotados = new HashMap<>(ativos.buscarTodos(tickers));
        List<PosicaoValorizada> valorizadas = new ArrayList<>();
        BigDecimal valorTotal = BigDecimal.ZERO;
        BigDecimal custoTotal = BigDecimal.ZERO;
        for (Posicao posicao : posicoes) {
            if (posicao.quantidade() <= 0) {
                continue;
            }
            AtivoCotado ativo = cotados.get(posicao.ticker());
            if (ativo == null) {
                ativo = consultarNoCatalogo(posicao.ticker());
            }
            PosicaoValorizada valorizada = valorizar(posicao, ativo);
            valorizadas.add(valorizada);
            valorTotal = valorTotal.add(valorizada.valorDeMercado());
            custoTotal = custoTotal.add(valorizada.custo());
        }
        return new CarteiraValorizada(clienteId, valorizadas, valorTotal, custoTotal);
    }

    private AtivoCotado consultarNoCatalogo(String ticker) {
        Optional<AtivoCotado> ativo = catalogo.consultar(ticker);
        if (ativo.isEmpty()) {
            return null;
        }
        ativos.salvar(ativo.get());
        return ativo.get();
    }

    private static PosicaoValorizada valorizar(Posicao posicao, AtivoCotado ativo) {
        BigDecimal quantidade = BigDecimal.valueOf(posicao.quantidade());
        BigDecimal cotacao = ativo == null ? posicao.precoMedio() : ativo.cotacao();
        BigDecimal cambio = ativo == null ? BigDecimal.ONE : ativo.cambio();
        BigDecimal valor = cotacao.multiply(quantidade).multiply(cambio).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal custo = posicao.precoMedio().multiply(quantidade).multiply(cambio).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal resultado = custo.signum() == 0 ? BigDecimal.ZERO
                : valor.divide(custo, 6, RoundingMode.HALF_EVEN).subtract(BigDecimal.ONE)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_EVEN);
        return new PosicaoValorizada(posicao.ticker(), ativo == null ? posicao.ticker() : ativo.nome(),
                posicao.quantidade(), posicao.quantidadeReservada(), posicao.precoMedio(), cotacao,
                ativo == null ? null : ativo.moeda(), valor, custo, resultado);
    }
}
