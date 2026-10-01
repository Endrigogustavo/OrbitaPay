package com.orbitapay.auth.domain.exception;

public class PinIncorretoException extends RuntimeException {

    private final int tentativasRestantes;
    private final boolean bloqueado;

    public PinIncorretoException(int tentativasRestantes, boolean bloqueado) {
        super(bloqueado
                ? "Conta bloqueada após tentativas de PIN incorretas"
                : "PIN incorreto · " + tentativasRestantes
                        + (tentativasRestantes == 1 ? " tentativa restante" : " tentativas restantes"));
        this.tentativasRestantes = tentativasRestantes;
        this.bloqueado = bloqueado;
    }

    public int tentativasRestantes() {
        return tentativasRestantes;
    }

    public boolean bloqueado() {
        return bloqueado;
    }
}
