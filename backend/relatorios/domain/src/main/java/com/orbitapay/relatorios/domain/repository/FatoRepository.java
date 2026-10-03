package com.orbitapay.relatorios.domain.repository;

import java.util.List;

import com.orbitapay.relatorios.domain.model.Fato;
import com.orbitapay.relatorios.domain.model.Periodo;

public interface FatoRepository {

    boolean registrar(Fato fato);

    List<Fato> listarNoPeriodo(Periodo periodo);

    List<Fato> listarDoClienteNoPeriodo(String clienteId, Periodo periodo);
}
