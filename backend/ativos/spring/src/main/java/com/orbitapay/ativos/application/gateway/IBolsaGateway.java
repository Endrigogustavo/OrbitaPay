package com.orbitapay.ativos.application.gateway;

import java.util.List;
import java.util.Optional;

import com.orbitapay.ativos.entity.Bolsa;

public interface IBolsaGateway {

    void salvar(Bolsa bolsa);

    Optional<Bolsa> buscarPorCodigo(String codigo);

    List<Bolsa> listarTodas();
}
