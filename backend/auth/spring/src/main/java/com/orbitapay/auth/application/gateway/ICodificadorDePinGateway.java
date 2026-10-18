package com.orbitapay.auth.application.gateway;

public interface ICodificadorDePinGateway {

    String codificarPin(String pin);

    boolean pinConfereComOCodificado(String pin, String pinCodificado);
}
