package com.orbitapay.gateway.infrastructure.config;

import java.time.Clock;
import java.util.List;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.orbitapay.gateway.application.ControleDeAcesso;
import com.orbitapay.gateway.domain.VerificadorDeCredencial;
import com.orbitapay.gateway.web.FiltroDeAcesso;

@Configuration
public class GatewayConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    public ControleDeAcesso controleDeAcesso(VerificadorDeCredencial verificador, Clock relogio) {
        return new ControleDeAcesso(verificador, relogio);
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> filtroDeCors() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Location"));
        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/api/**", cors);
        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(origem));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }

    @Bean
    public FilterRegistrationBean<FiltroDeAcesso> filtroDeAcesso(ControleDeAcesso controleDeAcesso) {
        FilterRegistrationBean<FiltroDeAcesso> registro = new FilterRegistrationBean<>(
                new FiltroDeAcesso(controleDeAcesso));
        registro.addUrlPatterns("/api/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registro;
    }
}
