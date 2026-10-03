package com.orbitapay.pagamentos.provedor;

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
