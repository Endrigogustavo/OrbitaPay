package com.orbitapay.ativos.infrastructure.seed;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.entity.Bolsa;
import com.orbitapay.ativos.application.gateway.IAtivoGateway;
import com.orbitapay.ativos.application.gateway.IBolsaGateway;
import com.orbitapay.ativos.application.usecase.AtivoUseCase;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class DadosIniciais implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private final AtivoUseCase ativoUseCase;
    private final IAtivoGateway ativoGateway;
    private final IBolsaGateway bolsaGateway;

    public DadosIniciais(AtivoUseCase ativoUseCase, IAtivoGateway ativoGateway, IBolsaGateway bolsaGateway) {
        this.ativoUseCase = ativoUseCase;
        this.ativoGateway = ativoGateway;
        this.bolsaGateway = bolsaGateway;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bolsaGateway.listarTodas().isEmpty()) {
            cadastrarBolsas();
        }
        if (ativoGateway.listarTodos().isEmpty()) {
            listarAtivosDeDemonstracao();
        }
    }

    private void cadastrarBolsas() {
        cadastrarBolsaDeDemonstracao("B3", "B3 · Brasil, Bolsa, Balcão", "São Paulo", "BRL", "1", "America/Sao_Paulo", "10:00", "17:00");
        cadastrarBolsaDeDemonstracao("NYC", "NYSE · Nasdaq", "Nova York", "USD", "5.35", "America/New_York", "09:30", "16:00");
        cadastrarBolsaDeDemonstracao("TSX", "Toronto Stock Exchange", "Toronto", "CAD", "3.9", "America/Toronto", "09:30", "16:00");
        cadastrarBolsaDeDemonstracao("LSE", "London Stock Exchange", "Londres", "GBP", "7.2", "Europe/London", "08:00", "16:30");
        cadastrarBolsaDeDemonstracao("EPA", "Euronext Paris", "Paris", "EUR", "6.25", "Europe/Paris", "09:00", "17:30");
        cadastrarBolsaDeDemonstracao("FRA", "Deutsche Börse", "Frankfurt", "EUR", "6.25", "Europe/Berlin", "09:00", "17:30");
        cadastrarBolsaDeDemonstracao("JSE", "Johannesburg Stock Exchange", "Joanesburgo", "ZAR", "0.3", "Africa/Johannesburg", "09:00", "17:00");
        cadastrarBolsaDeDemonstracao("NSE", "National Stock Exchange", "Mumbai", "INR", "0.064", "Asia/Kolkata", "09:15", "15:30");
        cadastrarBolsaDeDemonstracao("SSE", "Shanghai Stock Exchange", "Xangai", "CNY", "0.75", "Asia/Shanghai", "09:30", "15:00");
        cadastrarBolsaDeDemonstracao("HKEX", "Hong Kong Exchanges", "Hong Kong", "HKD", "0.69", "Asia/Hong_Kong", "09:30", "16:00");
        cadastrarBolsaDeDemonstracao("TSE", "Tokyo Stock Exchange", "Tóquio", "JPY", "0.036", "Asia/Tokyo", "09:00", "15:00");
        cadastrarBolsaDeDemonstracao("ASX", "Australian Securities Exchange", "Sydney", "AUD", "3.55", "Australia/Sydney", "10:00", "16:00");
        LOG.info("Bolsas de demonstração cadastradas");
    }

    private void listarAtivosDeDemonstracao() {
        listarAtivoDeDemonstracao("ORBT3", "Órbita Holding", "Financeiro", "B3", "10.00", 10L);
        listarAtivoDeDemonstracao("PETR4", "Petrobras", "Energia", "B3", "38.20", null);
        listarAtivoDeDemonstracao("VALE3", "Vale", "Mineração", "B3", "61.40", null);
        listarAtivoDeDemonstracao("ITUB4", "Itaú Unibanco", "Financeiro", "B3", "34.10", null);
        listarAtivoDeDemonstracao("WEGE3", "WEG", "Industrial", "B3", "41.80", null);
        listarAtivoDeDemonstracao("AAPL", "Apple", "Tecnologia", "NYC", "228.50", null);
        listarAtivoDeDemonstracao("NVDA", "NVIDIA", "Semicondutores", "NYC", "176.30", null);
        listarAtivoDeDemonstracao("MSFT", "Microsoft", "Tecnologia", "NYC", "512.40", null);
        listarAtivoDeDemonstracao("KO", "Coca-Cola", "Consumo", "NYC", "68.20", null);
        listarAtivoDeDemonstracao("SHOP", "Shopify", "Tecnologia", "TSX", "210.40", null);
        listarAtivoDeDemonstracao("SHEL", "Shell", "Energia", "LSE", "27.10", null);
        listarAtivoDeDemonstracao("AZN", "AstraZeneca", "Saúde", "LSE", "110.20", null);
        listarAtivoDeDemonstracao("MC", "LVMH", "Luxo", "EPA", "520.30", null);
        listarAtivoDeDemonstracao("AIR", "Airbus", "Aeroespacial", "EPA", "190.60", null);
        listarAtivoDeDemonstracao("SAP", "SAP", "Software", "FRA", "238.40", null);
        listarAtivoDeDemonstracao("SIE", "Siemens", "Industrial", "FRA", "224.90", null);
        listarAtivoDeDemonstracao("NPN", "Naspers", "Tecnologia", "JSE", "4980.00", null);
        listarAtivoDeDemonstracao("RELIANCE", "Reliance Industries", "Energia", "NSE", "1402.00", null);
        listarAtivoDeDemonstracao("600519", "Kweichow Moutai", "Consumo", "SSE", "1450.00", null);
        listarAtivoDeDemonstracao("0700", "Tencent", "Tecnologia", "HKEX", "602.00", null);
        listarAtivoDeDemonstracao("7203", "Toyota", "Automotivo", "TSE", "2810.00", null);
        listarAtivoDeDemonstracao("6758", "Sony", "Tecnologia", "TSE", "3920.00", null);
        listarAtivoDeDemonstracao("BHP", "BHP Group", "Mineração", "ASX", "42.30", null);
        LOG.info("Ativos de demonstração listados");
    }

    private void cadastrarBolsaDeDemonstracao(String codigo, String nome, String cidade, String moeda, String cambio, String fuso,
            String abertura, String fechamento) {
        bolsaGateway.salvar(new Bolsa(codigo, nome, cidade, moeda, new BigDecimal(cambio), fuso, abertura, fechamento));
    }

    private void listarAtivoDeDemonstracao(String ticker, String nome, String setor, String bolsa, String cotacao, Long quantidadeEmitida) {
        ativoUseCase.listarNovoAtivoNaBolsa(ticker, nome, setor, bolsa, new BigDecimal(cotacao), quantidadeEmitida);
    }
}
