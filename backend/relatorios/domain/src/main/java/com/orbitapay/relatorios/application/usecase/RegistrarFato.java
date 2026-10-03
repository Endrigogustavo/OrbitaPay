package com.orbitapay.relatorios.application.usecase;

import java.util.Optional;

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

    public void executar(Fato fato, String nome) {
        if (fatos.registrar(fato)) {
            atualizarPerfil(fato, nome);
        }
    }

    public void renomearCliente(String clienteId, String nome) {
        Optional<PerfilDeCliente> perfil = perfis.buscar(clienteId);
        if (perfil.isPresent()) {
            perfil.get().renomear(nome);
            perfis.salvar(perfil.get());
        }
    }

    private void atualizarPerfil(Fato fato, String nome) {
        Optional<PerfilDeCliente> perfil = perfis.buscar(fato.clienteId());
        if (fato.tipo() == TipoDeFato.CLIENTE_CADASTRADO && perfil.isEmpty()) {
            perfis.salvar(PerfilDeCliente.cadastrado(fato.clienteId(), nome, fato.ocorridoEm()));
            return;
        }
        if (perfil.isEmpty()) {
            return;
        }
        if (fato.tipo() == TipoDeFato.CLIENTE_BLOQUEADO) {
            perfil.get().alterarSituacao(true);
            perfis.salvar(perfil.get());
        } else if (fato.tipo() == TipoDeFato.CLIENTE_DESBLOQUEADO) {
            perfil.get().alterarSituacao(false);
            perfis.salvar(perfil.get());
        } else if (fato.tipo() == TipoDeFato.CLIENTE_REMOVIDO) {
            perfil.get().encerrar();
            perfis.salvar(perfil.get());
        }
    }
}
