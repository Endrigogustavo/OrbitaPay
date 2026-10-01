package com.orbitapay.ativos.infrastructure.seed;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.orbitapay.ativos.application.usecase.ListarAtivoNaBolsa;
import com.orbitapay.ativos.domain.model.Bolsa;
import com.orbitapay.ativos.domain.repository.AtivoRepository;
import com.orbitapay.ativos.domain.repository.BolsaRepository;

@Component
@ConditionalOnProperty(name = "orbita.dados-iniciais", havingValue = "true")
public class DadosIniciais implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DadosIniciais.class);

    private static final List<Bolsa> BOLSAS = List.of(
            bolsa("B3", "B3 · Brasil, Bolsa, Balcão", "São Paulo", "BRL", "1", "America/Sao_Paulo", "10:00", "17:00"),
            bolsa("NYC", "NYSE · Nasdaq", "Nova York", "USD", "5.35", "America/New_York", "09:30", "16:00"),
            bolsa("TSX", "Toronto Stock Exchange", "Toronto", "CAD", "3.9", "America/Toronto", "09:30", "16:00"),
            bolsa("LSE", "London Stock Exchange", "Londres", "GBP", "7.2", "Europe/London", "08:00", "16:30"),
            bolsa("EPA", "Euronext Paris", "Paris", "EUR", "6.25", "Europe/Paris", "09:00", "17:30"),
            bolsa("FRA", "Deutsche Börse", "Frankfurt", "EUR", "6.25", "Europe/Berlin", "09:00", "17:30"),
            bolsa("JSE", "Johannesburg Stock Exchange", "Joanesburgo", "ZAR", "0.3", "Africa/Johannesburg", "09:00", "17:00"),
            bolsa("NSE", "National Stock Exchange", "Mumbai", "INR", "0.064", "Asia/Kolkata", "09:15", "15:30"),
            bolsa("SSE", "Shanghai Stock Exchange", "Xangai", "CNY", "0.75", "Asia/Shanghai", "09:30", "15:00"),
            bolsa("HKEX", "Hong Kong Exchanges", "Hong Kong", "HKD", "0.69", "Asia/Hong_Kong", "09:30", "16:00"),
            bolsa("TSE", "Tokyo Stock Exchange", "Tóquio", "JPY", "0.036", "Asia/Tokyo", "09:00", "15:00"),
            bolsa("ASX", "Australian Securities Exchange", "Sydney", "AUD", "3.55", "Australia/Sydney", "10:00", "16:00"));

    private static final List<ListarAtivoNaBolsa.Comando> ATIVOS = List.of(
            ativo("ORBT3", "Órbita Holding", "Financeiro", "B3", "10.00", 10L),
            ativo("PETR4", "Petrobras", "Energia", "B3", "38.20", null),
            ativo("VALE3", "Vale", "Mineração", "B3", "61.40", null),
            ativo("ITUB4", "Itaú Unibanco", "Financeiro", "B3", "34.10", null),
            ativo("WEGE3", "WEG", "Industrial", "B3", "41.80", null),
            ativo("AAPL", "Apple", "Tecnologia", "NYC", "228.50", null),
            ativo("NVDA", "NVIDIA", "Semicondutores", "NYC", "176.30", null),
            ativo("MSFT", "Microsoft", "Tecnologia", "NYC", "512.40", null),
            ativo("KO", "Coca-Cola", "Consumo", "NYC", "68.20", null),
            ativo("SHOP", "Shopify", "Tecnologia", "TSX", "210.40", null),
            ativo("SHEL", "Shell", "Energia", "LSE", "27.10", null),
            ativo("AZN", "AstraZeneca", "Saúde", "LSE", "110.20", null),
            ativo("MC", "LVMH", "Luxo", "EPA", "520.30", null),
            ativo("AIR", "Airbus", "Aeroespacial", "EPA", "190.60", null),
            ativo("SAP", "SAP", "Software", "FRA", "238.40", null),
            ativo("SIE", "Siemens", "Industrial", "FRA", "224.90", null),
            ativo("NPN", "Naspers", "Tecnologia", "JSE", "4980.00", null),
            ativo("RELIANCE", "Reliance Industries", "Energia", "NSE", "1402.00", null),
            ativo("600519", "Kweichow Moutai", "Consumo", "SSE", "1450.00", null),
            ativo("0700", "Tencent", "Tecnologia", "HKEX", "602.00", null),
            ativo("7203", "Toyota", "Automotivo", "TSE", "2810.00", null),
            ativo("6758", "Sony", "Tecnologia", "TSE", "3920.00", null),
            ativo("BHP", "BHP Group", "Mineração", "ASX", "42.30", null));

    private final AtivoRepository ativos;
    private final BolsaRepository bolsas;
    private final ListarAtivoNaBolsa listarAtivo;

    public DadosIniciais(AtivoRepository ativos, BolsaRepository bolsas, ListarAtivoNaBolsa listarAtivo) {
        this.ativos = ativos;
        this.bolsas = bolsas;
        this.listarAtivo = listarAtivo;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (bolsas.listar().isEmpty()) {
            BOLSAS.forEach(bolsas::salvar);
            LOG.info("{} bolsas cadastradas", BOLSAS.size());
        }
        if (ativos.vazio()) {
            ATIVOS.forEach(listarAtivo::executar);
            LOG.info("{} ativos listados", ATIVOS.size());
        }
    }

    private static Bolsa bolsa(String codigo, String nome, String cidade, String moeda, String cambio, String fuso,
            String abertura, String fechamento) {
        return new Bolsa(codigo, nome, cidade, moeda, new BigDecimal(cambio), fuso, LocalTime.parse(abertura),
                LocalTime.parse(fechamento));
    }

    private static ListarAtivoNaBolsa.Comando ativo(String ticker, String nome, String setor, String bolsa,
            String cotacao, Long emitidas) {
        return new ListarAtivoNaBolsa.Comando(ticker, nome, setor, bolsa, new BigDecimal(cotacao), emitidas);
    }
}
