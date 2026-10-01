package com.orbitapay.relatorios.application.usecase;

import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.PerfilDeCliente;
import com.orbitapay.relatorios.domain.model.TipoDeFato;
import com.orbitapay.relatorios.domain.repository.FatoRepository;
import com.orbitapay.relatorios.domain.repository.PerfilDeClienteRepository;

public class RegistrarFato {

    private final FatoRepository fatos;
    private final PerfilDeClienteRepository perfis;

    public RegistrarFato(FatoRepository fatos, PerfilDeClienteRepository perfis) {
        this.fatos = fatos;
        this.perfis = perfis;
    }

    /** @param nome nome do cliente, quando a mensagem de origem trouxer (cadastro e atualização) */
    public void executar(Fato fato, String nome) {
        if (fatos.registrar(fato)) {
            atualizarPerfil(fato, nome);
        }
    }

    /** Atualizações cadastrais não contam como fato, mas mantêm o nome usado nos rankings. */
    public void renomearCliente(String clienteId, String nome) {
        perfis.buscar(clienteId).ifPresent(perfil -> {
            perfil.renomear(nome);
            perfis.salvar(perfil);
        });
    }

    private void atualizarPerfil(Fato fato, String nome) {
        switch (fato.tipo()) {
            case CLIENTE_CADASTRADO -> perfis.salvar(perfis.buscar(fato.clienteId())
                    .orElseGet(() -> PerfilDeCliente.cadastrado(fato.clienteId(), nome, fato.ocorridoEm())));
            case CLIENTE_BLOQUEADO, CLIENTE_DESBLOQUEADO -> perfis.buscar(fato.clienteId()).ifPresent(perfil -> {
                perfil.alterarSituacao(fato.tipo() == TipoDeFato.CLIENTE_BLOQUEADO);
                perfis.salvar(perfil);
            });
            case CLIENTE_REMOVIDO -> perfis.buscar(fato.clienteId()).ifPresent(perfil -> {
                perfil.encerrar();
                perfis.salvar(perfil);
            });
            default -> {
            }
        }
    }
}
