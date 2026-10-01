package com.orbitapay.ativos.domain.repository;

import java.util.List;
import java.util.Optional;

import com.orbitapay.ativos.domain.model.Bolsa;

public interface BolsaRepository {

    Optional<Bolsa> buscar(String codigo);

    List<Bolsa> listar();

    void salvar(Bolsa bolsa);
}
