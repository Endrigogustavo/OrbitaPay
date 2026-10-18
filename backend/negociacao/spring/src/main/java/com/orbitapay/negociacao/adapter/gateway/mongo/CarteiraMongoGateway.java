package com.orbitapay.negociacao.adapter.gateway.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.orbitapay.negociacao.entity.Carteira;
import com.orbitapay.negociacao.entity.Posicao;
import com.orbitapay.negociacao.application.gateway.ICarteiraGateway;
import com.orbitapay.negociacao.adapter.gateway.mongo.document.CarteiraDocument;
import com.orbitapay.negociacao.adapter.gateway.mongo.document.PosicaoDocument;
import com.orbitapay.negociacao.adapter.gateway.mongo.repository.CarteiraRepository;

@Component
public class CarteiraMongoGateway implements ICarteiraGateway {

    private final CarteiraRepository carteiraRepository;

    public CarteiraMongoGateway(CarteiraRepository carteiraRepository) {
        this.carteiraRepository = carteiraRepository;
    }

    @Override
    public void salvar(Carteira carteira) {
        List<PosicaoDocument> posicoes = carteira.posicoes().stream()
                .map(posicao -> new PosicaoDocument(posicao.ticker(), posicao.quantidade(), posicao.precoMedio()))
                .toList();
        carteiraRepository.save(new CarteiraDocument(carteira.clienteId(), posicoes));
    }

    @Override
    public Optional<Carteira> buscarPorCliente(String clienteId) {
        return carteiraRepository.findById(clienteId).map(documento -> new Carteira(documento.clienteId(),
                documento.posicoes().stream()
                        .map(posicao -> new Posicao(posicao.ticker(), posicao.quantidade(), posicao.precoMedio()))
                        .toList()));
    }

    @Override
    public void removerPorCliente(String clienteId) {
        carteiraRepository.deleteById(clienteId);
    }
}
