package com.orbitapay.contas.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.orbitapay.contas.domain.model.Conta;
import com.orbitapay.contas.domain.model.Dinheiro;
import com.orbitapay.contas.domain.model.Lancamento;
import com.orbitapay.contas.domain.model.TipoLancamento;
import com.orbitapay.contas.domain.repository.ContaRepository;
import com.orbitapay.contas.domain.repository.ContaTravada;
import com.orbitapay.contas.domain.repository.TravaDeConta;
import com.orbitapay.contas.persistence.trava.TravaPessimistaMongo;

@Component
public class MongoContaRepository implements ContaRepository, TravaDeConta {

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
    public ContaTravada travar(String clienteId) {
        String dono = UUID.randomUUID().toString();
        ContaDocument documento = trava.travar(clienteId, dono);
        return new ContaTravada(paraDominio(documento), dono);
    }

    @Override
    public void salvarELiberar(ContaTravada travada) {
        trava.salvarELiberar(paraDocumento(travada.conta()), travada.dono());
    }

    @Override
    public void liberar(ContaTravada travada) {
        trava.liberar(travada.conta().clienteId(), travada.dono());
    }

    @Override
    public void removerPorCliente(String clienteId) {
        mongo.deleteByClienteId(clienteId);
    }

    private static ContaDocument paraDocumento(Conta conta) {
        List<LancamentoDocument> lancamentos = new ArrayList<>();
        for (Lancamento l : conta.lancamentos()) {
            lancamentos.add(new LancamentoDocument(l.id(), l.tipo().name(), l.valor().valor(), l.descricao(),
                    l.referencia(), l.ocorridoEm()));
        }
        return new ContaDocument(conta.id(), conta.clienteId(), conta.nomeTitular(), conta.titularBloqueado(),
                conta.numero(), conta.saldo().valor(), lancamentos, conta.abertaEm(), null);
    }

    private static Conta paraDominio(ContaDocument documento) {
        List<Lancamento> lancamentos = new ArrayList<>();
        if (documento.lancamentos() != null) {
            for (LancamentoDocument l : documento.lancamentos()) {
                lancamentos.add(new Lancamento(l.id(), TipoLancamento.valueOf(l.tipo()), new Dinheiro(l.valor()),
                        l.descricao(), l.referencia(), l.ocorridoEm()));
            }
        }
        return new Conta(documento.id(), documento.clienteId(), documento.nomeTitular(),
                documento.titularBloqueado(), documento.numero(), new Dinheiro(documento.saldo()), lancamentos,
                documento.abertaEm());
    }
}
