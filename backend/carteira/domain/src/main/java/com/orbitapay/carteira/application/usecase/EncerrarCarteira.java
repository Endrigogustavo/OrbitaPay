package com.orbitapay.carteira.application.usecase;

import com.orbitapay.carteira.domain.repository.CarteiraRepository;

public class EncerrarCarteira {

    private final CarteiraRepository carteiras;

    public EncerrarCarteira(CarteiraRepository carteiras) {
        this.carteiras = carteiras;
    }

    public void executar(String clienteId) {
        carteiras.removerPorCliente(clienteId);
    }
}
