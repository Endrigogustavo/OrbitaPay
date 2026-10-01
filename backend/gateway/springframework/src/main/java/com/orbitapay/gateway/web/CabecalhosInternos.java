package com.orbitapay.gateway.web;

import java.util.Set;

public final class CabecalhosInternos {

    public static final String CLIENTE_ID = "X-Cliente-Id";
    public static final String PERFIL = "X-Perfil";
    public static final String ASSINATURA = "X-Assinatura";

    public static final Set<String> CONTROLADOS_PELO_GATEWAY = Set.of(
            CLIENTE_ID.toLowerCase(), PERFIL.toLowerCase(), ASSINATURA.toLowerCase(), "authorization");

    private CabecalhosInternos() {
    }
}
