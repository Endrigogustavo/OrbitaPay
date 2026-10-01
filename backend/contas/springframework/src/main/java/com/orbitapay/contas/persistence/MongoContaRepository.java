package com.orbitapay.contas.persistence;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.application.exception.TravaIndisponivelException;
import com.orbitapay.contas.domain.exception.ContaNaoEncontradaException;
import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.model.Lancamento;
import com.orbitapay.contas.domain.model.TipoLancamento;
import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.persistence.trava.TravaPessimistaMongo;

@Component
public class MongoContaRepository implements ContaRepository {

    private final ContaDocumentRepository mongo;
    private final TravaPessimistaMongo trava;

    public MongoContaRepository(ContaDocumentRepository mongo, TravaPessimistaMongo trava) {
        this.mongo = mongo;
        this.trava = trava;
    }

    @Override
    public String proximoId() {
        return new ObjectId().toHexString();
    }

    @Override
    public boolean existePorCliente(String clienteId) {
        return mongo.existsByClienteId(clienteId);
    }

    @Override
    public Optional<Conta> buscarPorCliente(String clienteId) {
        return mongo.findByClienteId(clienteId).map(MongoContaRepository::paraDominio);
    }

    @Override
    public List<Conta> listar() {
        return mongo.findAll(Sort.by("abertaEm")).stream().map(MongoContaRepository::paraDominio).toList();
    }

    @Override
    public void inserir(Conta conta) {
        mongo.insert(paraDocumento(conta));
    }

    @Override
    public ContaTravada travarPorCliente(String clienteId) {
        String dono = TravaPessimistaMongo.novoDono();
        ContaDocument documento = trava.adquirir(ContaDocument.class, porCliente(clienteId), recurso(clienteId), dono);
        if (documento == null) {
            throw new ContaNaoEncontradaException(clienteId);
        }
        return new ContaTravada(paraDominio(documento), dono);
    }

    @Override
    public void salvarELiberar(ContaTravada travada) {
        String clienteId = travada.conta().clienteId();
        boolean salvou = trava.substituirELiberar(ContaDocument.class, porCliente(clienteId), recurso(clienteId),
                travada.dono(), paraDocumento(travada.conta()));
        if (!salvou) {
            throw new TravaIndisponivelException(recurso(clienteId));
        }
    }

    @Override
    public void liberar(ContaTravada travada) {
        String clienteId = travada.conta().clienteId();
        trava.liberar(ContaDocument.class, porCliente(clienteId), recurso(clienteId), travada.dono());
    }

    @Override
    public void removerPorCliente(String clienteId) {
        mongo.deleteByClienteId(clienteId);
    }

    private static Criteria porCliente(String clienteId) {
        return Criteria.where("clienteId").is(clienteId);
    }

    private static String recurso(String clienteId) {
        return "conta:" + clienteId;
    }

    private static ContaDocument paraDocumento(Conta conta) {
        List<LancamentoDocument> lancamentos = conta.lancamentos().stream()
                .map(l -> new LancamentoDocument(l.id(), l.tipo().name(), l.valor().valor(), l.descricao(),
                        l.referencia(), l.ocorridoEm()))
                .toList();
        return new ContaDocument(conta.id(), conta.clienteId(), conta.nomeTitular(), conta.titularBloqueado(),
                conta.numero(), conta.saldo().valor(), lancamentos, conta.abertaEm(), null);
    }

    private static Conta paraDominio(ContaDocument documento) {
        List<Lancamento> lancamentos = documento.lancamentos() == null ? List.of()
                : documento.lancamentos().stream()
                        .map(l -> new Lancamento(l.id(), TipoLancamento.valueOf(l.tipo()), new Dinheiro(l.valor()),
                                l.descricao(), l.referencia(), l.ocorridoEm()))
                        .toList();
        return Conta.reconstituir(documento.id(), documento.clienteId(), documento.nomeTitular(),
                documento.titularBloqueado(), documento.numero(), new Dinheiro(documento.saldo()), lancamentos,
                documento.abertaEm());
    }
}
