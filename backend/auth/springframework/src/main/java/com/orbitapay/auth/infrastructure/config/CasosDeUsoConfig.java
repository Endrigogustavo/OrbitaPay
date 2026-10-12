package com.orbitapay.auth.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitapay.auth.application.service.CodificadorDePin;
import com.orbitapay.auth.application.service.EmissorDeToken;
import com.orbitapay.auth.application.service.PublicadorDeEventosDeCredencial;
import com.orbitapay.auth.application.usecase.AlterarPin;
import com.orbitapay.auth.application.usecase.AssinarOperacao;
import com.orbitapay.auth.application.usecase.AtualizarEmailDaCredencial;
import com.orbitapay.auth.application.usecase.AutenticarCliente;
import com.orbitapay.auth.application.usecase.AutenticarFuncionario;
import com.orbitapay.auth.application.usecase.CadastrarCredencial;
import com.orbitapay.auth.application.usecase.CadastrarFuncionario;
import com.orbitapay.auth.application.usecase.ConferenciaDePin;
import com.orbitapay.auth.application.usecase.ConsultarCredencial;
import com.orbitapay.auth.application.usecase.DesfazerCadastroDeCredencial;
import com.orbitapay.auth.application.usecase.Entrar;
import com.orbitapay.auth.application.usecase.RegistrarCredencial;
import com.orbitapay.auth.application.usecase.RemoverCredencial;
import com.orbitapay.auth.application.usecase.TornarGerente;
import com.orbitapay.auth.application.usecase.ZerarTentativasDePin;
import com.orbitapay.auth.domain.repository.CredencialRepository;
import com.orbitapay.auth.domain.repository.FuncionarioRepository;

@Configuration
public class CasosDeUsoConfig {

    @Bean
    public ConferenciaDePin conferenciaDePin(CredencialRepository repositorio, CodificadorDePin codificador,
            PublicadorDeEventosDeCredencial publicador) {
        return new ConferenciaDePin(repositorio, codificador, publicador);
    }

    @Bean
    public RegistrarCredencial registrarCredencial(CredencialRepository repositorio,
            FuncionarioRepository funcionarios, CodificadorDePin codificador) {
        return new RegistrarCredencial(repositorio, funcionarios, codificador);
    }

    @Bean
    public CadastrarCredencial cadastrarCredencial(CredencialRepository repositorio,
            RegistrarCredencial registrarCredencial, EmissorDeToken emissor) {
        return new CadastrarCredencial(repositorio, registrarCredencial, emissor);
    }

    @Bean
    public DesfazerCadastroDeCredencial desfazerCadastroDeCredencial(CredencialRepository repositorio) {
        return new DesfazerCadastroDeCredencial(repositorio);
    }

    @Bean
    public TornarGerente tornarGerente(CredencialRepository credenciais, FuncionarioRepository funcionarios,
            EmissorDeToken emissor) {
        return new TornarGerente(credenciais, funcionarios, emissor);
    }

    @Bean
    public AutenticarCliente autenticarCliente(CredencialRepository repositorio, ConferenciaDePin conferencia,
            EmissorDeToken emissor) {
        return new AutenticarCliente(repositorio, conferencia, emissor);
    }

    @Bean
    public AutenticarFuncionario autenticarFuncionario(FuncionarioRepository repositorio,
            CodificadorDePin codificador, EmissorDeToken emissor) {
        return new AutenticarFuncionario(repositorio, codificador, emissor);
    }

    @Bean
    public Entrar entrar(FuncionarioRepository funcionarios, AutenticarFuncionario autenticarFuncionario,
            AutenticarCliente autenticarCliente) {
        return new Entrar(funcionarios, autenticarFuncionario, autenticarCliente);
    }

    @Bean
    public CadastrarFuncionario cadastrarFuncionario(FuncionarioRepository funcionarios,
            CredencialRepository credenciais, CodificadorDePin codificador) {
        return new CadastrarFuncionario(funcionarios, credenciais, codificador);
    }

    @Bean
    public AssinarOperacao assinarOperacao(CredencialRepository repositorio, ConferenciaDePin conferencia,
            EmissorDeToken emissor) {
        return new AssinarOperacao(repositorio, conferencia, emissor);
    }

    @Bean
    public AlterarPin alterarPin(CredencialRepository repositorio, CodificadorDePin codificador) {
        return new AlterarPin(repositorio, codificador);
    }

    @Bean
    public ConsultarCredencial consultarCredencial(CredencialRepository repositorio) {
        return new ConsultarCredencial(repositorio);
    }

    @Bean
    public AtualizarEmailDaCredencial atualizarEmailDaCredencial(CredencialRepository repositorio) {
        return new AtualizarEmailDaCredencial(repositorio);
    }

    @Bean
    public ZerarTentativasDePin zerarTentativasDePin(CredencialRepository repositorio) {
        return new ZerarTentativasDePin(repositorio);
    }

    @Bean
    public RemoverCredencial removerCredencial(CredencialRepository repositorio) {
        return new RemoverCredencial(repositorio);
    }
}
