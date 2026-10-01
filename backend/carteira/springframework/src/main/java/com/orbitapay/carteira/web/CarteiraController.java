package com.orbitapay.carteira.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.orbitapay.carteira.application.dto.CarteiraValorizada;
import com.orbitapay.carteira.application.usecase.ConsultarCarteira;

@RestController
@RequestMapping("/carteiras")
public class CarteiraController {

    private static final String CLIENTE_ID = "X-Cliente-Id";
    private static final String PERFIL = "X-Perfil";

    private final ConsultarCarteira consultarCarteira;

    public CarteiraController(ConsultarCarteira consultarCarteira) {
        this.consultarCarteira = consultarCarteira;
    }

    @GetMapping("/me")
    public CarteiraValorizada minhaCarteira(@RequestHeader(CLIENTE_ID) String clienteId) {
        return consultarCarteira.doCliente(clienteId);
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<CarteiraValorizada> carteiraDoCliente(@PathVariable String clienteId,
            @RequestHeader(value = PERFIL, required = false) String perfil) {
        if (!"GERENTE".equals(perfil)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(consultarCarteira.doCliente(clienteId));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Void> semSessao() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
