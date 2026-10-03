package com.orbitapay.carteira.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.domain.model.Carteira;
import com.orbitapay.carteira.domain.model.Posicao;
import com.orbitapay.carteira.domain.repository.CarteiraRepository;
import com.orbitapay.carteira.domain.repository.CarteiraTravada;
import com.orbitapay.carteira.persistence.CarteiraDocument.PosicaoDocument;
import com.orbitapay.carteira.persistence.CarteiraDocument.ReservaDocument;
import com.orbitapay.carteira.persistence.trava.TravaPessimistaMongo;

@Component
public class MongoCarteiraRepository implements CarteiraRepository {

    private final CarteiraDocumentRepository mongo;
    private final TravaPessimistaMongo trava;

    public MongoCarteiraRepository(CarteiraDocumentRepository mongo, TravaPessimistaMongo trava) {
        this.mongo = mongo;
        this.trava = trava;
    }

    @Override
    public Optional<Carteira> buscarPorCliente(String clienteId) {
        return mongo.findByClienteId(clienteId).map(MongoCarteiraRepository::paraDominio);
    }

    @Override
    public CarteiraTravada travarPorCliente(String clienteId) {
        criarSeNaoExistir(clienteId);
        String dono = UUID.randomUUID().toString();
        CarteiraDocument documento = trava.travar(clienteId, dono);
        return new CarteiraTravada(paraDominio(documento), dono);
    }

    @Override
    public void salvarELiberar(CarteiraTravada travada) {
        trava.salvarELiberar(paraDocumento(travada.carteira()), travada.dono());
    }

    @Override
    public void liberar(CarteiraTravada travada) {
        trava.liberar(travada.carteira().clienteId(), travada.dono());
    }

    @Override
    public void removerPorCliente(String clienteId) {
        mongo.deleteByClienteId(clienteId);
    }

    private void criarSeNaoExistir(String clienteId) {
        if (mongo.existsByClienteId(clienteId)) {
            return;
        }
        try {
            mongo.insert(paraDocumento(Carteira.abrir(new ObjectId().toHexString(), clienteId)));
        } catch (DuplicateKeyException e) {
            return;
        }
    }

    private static CarteiraDocument paraDocumento(Carteira carteira) {
        List<PosicaoDocument> posicoes = new ArrayList<>();
        for (Posicao posicao : carteira.posicoes()) {
            List<ReservaDocument> reservas = new ArrayList<>();
            for (Map.Entry<String, Long> reserva : posicao.reservas().entrySet()) {
                reservas.add(new ReservaDocument(reserva.getKey(), reserva.getValue()));
            }
            posicoes.add(new PosicaoDocument(posicao.ticker(), posicao.quantidade(), posicao.precoMedio(), reservas));
        }
        return new CarteiraDocument(carteira.id(), carteira.clienteId(), posicoes,
                new ArrayList<>(carteira.ordensLiquidadas()), null);
    }

    private static Carteira paraDominio(CarteiraDocument documento) {
        List<Posicao> posicoes = new ArrayList<>();
        if (documento.posicoes() != null) {
            for (PosicaoDocument posicao : documento.posicoes()) {
                Map<String, Long> reservas = new HashMap<>();
                if (posicao.reservas() != null) {
                    for (ReservaDocument reserva : posicao.reservas()) {
                        reservas.put(reserva.ordemId(), reserva.quantidade());
                    }
                }
                posicoes.add(new Posicao(posicao.ticker(), posicao.quantidade(), posicao.precoMedio(), reservas));
            }
        }
        List<String> ordensLiquidadas = documento.ordensLiquidadas() == null ? List.of()
                : documento.ordensLiquidadas();
        return new Carteira(documento.id(), documento.clienteId(), posicoes, ordensLiquidadas);
    }
}
