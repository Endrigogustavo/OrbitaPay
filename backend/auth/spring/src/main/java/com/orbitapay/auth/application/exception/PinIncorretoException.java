package com.orbitapay.auth.application.exception;

public class PinIncorretoException extends RuntimeException {

    private final int tentativasRestantes;
    private final boolean bloqueado;

    public PinIncorretoException(int tentativasRestantes, boolean bloqueado) {
        super(bloqueado
                ? "PIN incorreto. Sua conta foi bloqueada por segurança"
                : "PIN incorreto. Restam " + tentativasRestantes + " tentativa(s)");
        this.tentativasRestantes = tentativasRestantes;
        this.bloqueado = bloqueado;
    }

    public int tentativasRestantes() {
        return tentativasRestantes;
    }

    public boolean contaFoiBloqueada() {
        return bloqueado;
    }
}
