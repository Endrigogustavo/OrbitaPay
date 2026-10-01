# Testes com k6

Scripts em JavaScript para o [k6](https://k6.io). Todos falam com o **gateway**, como o app mobile, e usam os clientes de demonstração (`ana@orbita.com` / `bruno@orbita.com`) ou cadastram investidores próprios.

| Script | O que faz | Critério de aprovação |
|---|---|---|
| `smoke.js` | Percorre uma vez cada bounded context: login, cadastro, ativos, conta, compra pela saga, carteira, depósito Pix (ACL) e relatórios | todas as verificações passam, nenhuma requisição falha |
| `concorrencia.js` | 20 compras simultâneas de 1 `ORBT3` (10 ações emitidas) e 10 saques simultâneos de R$ 500 na conta do Bruno | executadas = min(20, oferta); oferta final = inicial − executadas; saques = min(10, saldo ÷ 500); saldo nunca negativo; nenhum 409 |
| `carga-mercado.js` | Leituras públicas (ativos, ofertas, bolsas) em taxa de chegada constante | p95 < 300 ms, p99 < 800 ms, erros < 1% |
| `jornada-investidor.js` | N investidores simultâneos: cadastro → login → compras e vendas assinadas → depósito Pix → carteira e extrato | p95 de ordem concluída < 5 s, p95 de depósito confirmado < 10 s, erros < 2% |

`lib/orbita.js` concentra o acesso ao gateway (login, assinatura com PIN, envio e acompanhamento de ordens e pagamentos) e gera o resumo em texto de cada execução.

## Como rodar

Com o ecossistema no ar (`docker compose up --build -d`):

```bash
# com o k6 instalado na máquina
k6 run k6/smoke.js
k6 run k6/concorrencia.js
k6 run -e TAXA=200 -e DURACAO=2m k6/carga-mercado.js
k6 run -e INVESTIDORES=30 -e DURACAO=3m k6/jornada-investidor.js

# ou pelo container do compose (já aponta para http://gateway:8080)
docker compose run --rm k6 run smoke.js
docker compose run --rm k6 run concorrencia.js
```

| Variável | Padrão | Uso |
|---|---|---|
| `BASE_URL` | `http://localhost:8080` | endereço do gateway |
| `CODIGO_GERENTE` | `0000` | código do gerente (cadastro de investidores e relatório gerencial) |
| `TAXA`, `DURACAO` | `50`, `1m` | requisições por segundo e duração do patamar em `carga-mercado.js` |
| `INVESTIDORES`, `DURACAO` | `10`, `1m` | usuários virtuais e duração do patamar em `jornada-investidor.js` |

O teste de concorrência usa o estado atual como linha de base. Para reproduzir os números do enunciado (10 executadas, oferta final 0, 6 saques, saldo final R$ 150), rode-o num ambiente recém-criado (`docker compose down -v && docker compose up --build -d`). O relatório fica em `relatorio-concorrencia-<data>.txt`, na pasta onde o k6 rodou (no container, dentro de `k6/`).
