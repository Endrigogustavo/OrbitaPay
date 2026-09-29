package com.orbitapay.carteira.adapter.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.orbitapay.carteira.adapter.out.persistence.CarteiraDocument.PosicaoDocument;
import com.orbitapay.carteira.adapter.out.persistence.CarteiraDocument.ReservaDocument;
import com.orbitapay.carteira.adapter.out.persistence.trava.TravaPessimistaMongo;
import com.orbitapay.carteira.application.exception.TravaIndisponivelException;
import com.orbitapay.carteira.application.port.out.CarteiraRepository;
import com.orbitapay.carteira.application.port.out.CarteiraTravada;
import com.orbitapay.carteira.domain.model.Carteira;
import com.orbitapay.carteira.domain.model.Posicao;

@Component
public class CarteiraRepositoryMongoAdapter implements CarteiraRepository {

    private final CarteiraMongoRepository mongo;
    private final TravaPessimistaMongo trava;

    public CarteiraRepositoryMongoAdapter(CarteiraMongoRepository mongo, TravaPessimistaMongo trava) {
        this.mongo = mongo;
        this.trava = trava;
    }

    @Override
    public Optional<Carteira> buscarPorCliente(String clienteId) {
        return mongo.findByClienteId(clienteId).map(CarteiraRepositoryMongoAdapter::paraDominio);
    }

    @Override
    public CarteiraTravada travarPorCliente(String clienteId) {
        garantirExistencia(clienteId);
        String dono = TravaPessimistaMongo.novoDono();
        CarteiraDocument documento = trava.adquirir(CarteiraDocument.class, porCliente(clienteId),
                recurso(clienteId), dono);
        if (documento == null) {
            throw new TravaIndisponivelException(recurso(clienteId));
        }
        return new CarteiraTravada(paraDominio(documento), dono);
    }

    @Override
    public void salvarELiberar(CarteiraTravada travada) {
        String clienteId = travada.carteira().clienteId();
        if (!trava.substituirELiberar(CarteiraDocument.class, porCliente(clienteId), recurso(clienteId),
                travada.dono(), paraDocumento(travada.carteira()))) {
            throw new TravaIndisponivelException(recurso(clienteId));
        }
    }

    @Override
    public void liberar(CarteiraTravada travada) {
        String clienteId = travada.carteira().clienteId();
        trava.liberar(CarteiraDocument.class, porCliente(clienteId), recurso(clienteId), travada.dono());
    }

    @Override
    public void removerPorCliente(String clienteId) {
        mongo.deleteByClienteId(clienteId);
    }

    private void garantirExistencia(String clienteId) {
        if (mongo.existsByClienteId(clienteId)) {
            return;
        }
        try {
            mongo.insert(paraDocumento(Carteira.abrir(new ObjectId().toHexString(), clienteId)));
        } catch (DuplicateKeyException e) {
            return;
        }
    }

    private static Criteria porCliente(String clienteId) {
        return Criteria.where("clienteId").is(clienteId);
    }

    private static String recurso(String clienteId) {
        return "carteira:" + clienteId;
    }

    private static CarteiraDocument paraDocumento(Carteira carteira) {
        List<PosicaoDocument> posicoes = carteira.posicoes().stream()
                .map(p -> new PosicaoDocument(p.ticker(), p.quantidade(), p.precoMedio(), p.reservas().entrySet()
                        .stream().map(r -> new ReservaDocument(r.getKey(), r.getValue())).toList()))
                .toList();
        return new CarteiraDocument(carteira.id(), carteira.clienteId(), posicoes,
                List.copyOf(carteira.ordensLiquidadas()), null);
    }

    private static Carteira paraDominio(CarteiraDocument documento) {
        List<Posicao> posicoes = documento.posicoes() == null ? List.of()
                : documento.posicoes().stream().map(CarteiraRepositoryMongoAdapter::paraPosicao).toList();
        return Carteira.reconstituir(documento.id(), documento.clienteId(), posicoes,
                documento.ordensLiquidadas() == null ? List.of() : documento.ordensLiquidadas());
    }

    private static Posicao paraPosicao(PosicaoDocument documento) {
        Map<String, Long> reservas = documento.reservas() == null ? Map.of()
                : documento.reservas().stream().collect(Collectors.toMap(ReservaDocument::ordemId,
                        ReservaDocument::quantidade, (a, b) -> b));
        return new Posicao(documento.ticker(), documento.quantidade(), documento.precoMedio(), reservas);
    }
}
