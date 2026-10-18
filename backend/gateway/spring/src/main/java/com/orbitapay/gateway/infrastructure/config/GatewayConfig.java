package com.orbitapay.gateway.infrastructure.config;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.orbitapay.gateway.application.gateway.IAutenticacaoGateway;
import com.orbitapay.gateway.application.usecase.AcessoUseCase;
import com.orbitapay.gateway.service.PermissaoService;
import com.orbitapay.gateway.infrastructure.security.FiltroDeAcesso;

@Configuration
public class GatewayConfig {

    @Bean
    public PermissaoService permissaoService() {
        return new PermissaoService();
    }

    @Bean
    public AcessoUseCase acessoUseCase(PermissaoService permissaoService, IAutenticacaoGateway autenticacaoGateway) {
        return new AcessoUseCase(permissaoService, autenticacaoGateway);
    }

    @Bean
    public RestClient authRestClient(@Value("${orbita.servicos.auth}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(2));
        fabrica.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> filtroDeCors() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);
        cors.setExposedHeaders(List.of("Location"));
        UrlBasedCorsConfigurationSource origem = new UrlBasedCorsConfigurationSource();
        origem.registerCorsConfiguration("/api/**", cors);
        FilterRegistrationBean<CorsFilter> registro = new FilterRegistrationBean<>(new CorsFilter(origem));
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registro;
    }

    @Bean
    public FilterRegistrationBean<FiltroDeAcesso> filtroDeAcesso(AcessoUseCase acessoUseCase) {
        FilterRegistrationBean<FiltroDeAcesso> registro = new FilterRegistrationBean<>(new FiltroDeAcesso(acessoUseCase));
        registro.addUrlPatterns("/*");
        registro.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registro;
    }
}
