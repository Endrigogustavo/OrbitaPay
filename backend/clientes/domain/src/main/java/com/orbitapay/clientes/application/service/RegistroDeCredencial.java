package com.orbitapay.clientes.application.service;

/**
 * Pede ao contexto de Autenticação que crie a credencial (PIN) de um cliente recém-cadastrado. O PIN não é
 * guardado no contexto de Clientes.
 *
 * @throws com.orbitapay.clientes.domain.exception.RegraDeNegocioException se a Autenticação recusar o PIN
 * @throws AutenticacaoIndisponivelException se a Autenticação não responder
 */
public interface RegistroDeCredencial {

    void registrar(String clienteId, String email, String pin);
}
