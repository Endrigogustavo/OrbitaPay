package com.orbitapay.pagamentos.provedor;

/** Erro devolvido por uma API de provedor, no formato do provedor (status HTTP e código próprio). */
public class ErroDoProvedor extends RuntimeException {

    private final int status;
    private final String codigo;

    public ErroDoProvedor(int status, String codigo, String detalhe) {
        super(detalhe);
        this.status = status;
        this.codigo = codigo;
    }

    public int status() {
        return status;
    }

    public String codigo() {
        return codigo;
    }
}
