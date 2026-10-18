# Comunicação entre os serviços

Existem três formas de comunicação no OrbitaPay:

| Forma | Quem usa | Como funciona |
|---|---|---|
| **HTTP público** | app → gateway → serviço | O app só conhece o gateway (porta 9000). O gateway confere o acesso e repassa para o serviço dono da rota. |
| **HTTP interno** | serviço → serviço | Quando um serviço precisa de uma resposta na hora. As rotas começam com `/interno/` e não passam pelo gateway. |
| **Evento (RabbitMQ)** | serviço → exchange `orbita.eventos` → filas | Quando o trabalho pode acontecer depois e não pode se perder, ou quando vários serviços reagem ao mesmo fato. |

Cada serviço acessa **somente o seu próprio MongoDB**. Se precisa de um dado de outro serviço, pede pelo ID.

## 1. App → gateway → serviços

```mermaid
flowchart LR
    APP["App mobile"] -->|"HTTP :9000"| GW["gateway<br/>confere sessão e assinatura"]
    GW -->|"/api/autenticacao"| AUT["auth :9001"]
    GW -->|"/api/clientes · /api/contas"| CLI["clientes :9002"]
    GW -->|"/api/ativos · /api/bolsas"| ATV["ativos :9003"]
    GW -->|"/api/ordens · /api/carteiras"| NEG["negociacao :9004"]
    GW -->|"/api/pagamentos"| PAG["pagamentos :9005"]
    GW -.->|"GET /interno/acessos (a cada requisição com cookie)"| AUT
```

## 2. Chamadas HTTP entre serviços

```mermaid
flowchart LR
    AUT["auth"] -->|"POST /interno/clientes/{id}/bloqueio-por-pin"| CLI["clientes"]
    CLI -->|"PUT /interno/credenciais/{id}/email"| AUT
    NEG["negociacao"] -->|"GET /ativos/{ticker} · GET /ativos"| ATV["ativos"]
    NEG -->|"POST /interno/ativos/{ticker}/estoque/retiradas · /devolucoes"| ATV
    NEG -->|"GET /interno/clientes/{id}/situacao"| CLI
    NEG -->|"POST /interno/contas/{id}/compras · /vendas · /compras/{ordem}/estorno"| CLI
```

## 3. Eventos no RabbitMQ

```mermaid
flowchart LR
    NEG0["negociacao"] -->|"ordem.solicitada"| EX{{"exchange orbita.eventos"}}
    PAG0["pagamentos"] -->|"pagamento.confirmado"| EX
    CLI["clientes"] -->|"cliente.removido"| EX
    EX -->|"fila negociacao.ordens-solicitadas"| NEG1["negociacao<br/>executa a ordem"]
    EX -->|"fila clientes.pagamentos-confirmados"| CLI2["clientes<br/>credita o depósito"]
    EX -->|"fila auth.clientes-removidos"| AUT["auth<br/>remove credencial e sessões"]
    EX -->|"fila negociacao.clientes-removidos"| NEG["negociacao<br/>remove a carteira"]
    EX -->|"fila pagamentos.clientes-removidos"| PAG["pagamentos<br/>remove os pagamentos"]
```

| Evento | Publica | Fila | Consome | Para quê |
|---|---|---|---|---|
| `ordem.solicitada` | negociacao | `negociacao.ordens-solicitadas` | negociacao | executar a ordem em segundo plano (só se ainda estiver `PENDENTE`) |
| `pagamento.confirmado` | pagamentos | `clientes.pagamentos-confirmados` | clientes | creditar o depósito (uma única vez por `pagamentoId`) |
| `cliente.removido` | clientes | `auth.clientes-removidos` | auth | apagar a credencial e as sessões |
| `cliente.removido` | clientes | `negociacao.clientes-removidos` | negociacao | apagar a carteira |
| `cliente.removido` | clientes | `pagamentos.clientes-removidos` | pagamentos | apagar os pagamentos do cliente |

Se o consumo de uma mensagem falhar 3 vezes, ela vai para a fila `<fila>.erro` em vez de ficar em loop.

## 4. Cadastro de cliente

```mermaid
sequenceDiagram
    autonumber
    participant APP as App
    participant GW as gateway
    participant AUT as auth
    participant CLI as clientes
    APP->>GW: POST /api/autenticacao/credenciais {email, pin}
    GW->>AUT: repassa
    AUT-->>APP: 201 · clienteId + cookie orbita_sessao
    APP->>GW: POST /api/clientes {nome, email, cpf} (com o cookie)
    GW->>AUT: GET /interno/acessos (quem é?)
    GW->>CLI: repassa com X-Cliente-Id
    Note over CLI: transação: grava cliente + abre a conta
    CLI-->>APP: 201 Created
```

Se o cadastro for recusado (CPF inválido, e-mail repetido), o app desfaz a credencial com `DELETE /api/autenticacao/credenciais/me`, permitido até 10 minutos depois da criação.

## 5. Compra de ações

```mermaid
sequenceDiagram
    autonumber
    participant APP as App
    participant NEG as negociacao
    participant MQ as RabbitMQ
    participant ATV as ativos
    participant CLI as clientes
    APP->>NEG: POST /api/ordens {ticker, COMPRA, qtd} + X-Assinatura (via gateway)
    NEG->>CLI: GET /interno/clientes/{id}/situacao
    NEG->>ATV: GET /ativos/{ticker} (cotação e câmbio)
    Note over NEG: grava a ordem PENDENTE
    NEG->>MQ: ordem.solicitada
    NEG-->>APP: 202 · ordem PENDENTE
    MQ->>NEG: fila negociacao.ordens-solicitadas
    NEG->>ATV: POST /interno/ativos/{ticker}/estoque/retiradas
    NEG->>CLI: POST /interno/contas/{id}/compras
    alt saldo suficiente
        Note over NEG: transação: carteira += ações (preço médio) · ordem EXECUTADA
    else saldo insuficiente
        NEG->>ATV: POST /interno/ativos/{ticker}/estoque/devolucoes
        Note over NEG: ordem REJEITADA com o motivo
    end
    loop até sair de PENDENTE
        APP->>NEG: GET /api/ordens/{id}
    end
```

## 6. Venda de ações

```mermaid
sequenceDiagram
    autonumber
    participant APP as App
    participant NEG as negociacao
    participant MQ as RabbitMQ
    participant CLI as clientes
    participant ATV as ativos
    APP->>NEG: POST /api/ordens {ticker, VENDA, qtd} + X-Assinatura (via gateway)
    Note over NEG: grava a ordem PENDENTE
    NEG->>MQ: ordem.solicitada
    NEG-->>APP: 202 · ordem PENDENTE
    MQ->>NEG: fila negociacao.ordens-solicitadas
    Note over NEG: transação: carteira -= ações (recusa se não tiver)
    NEG->>CLI: POST /interno/contas/{id}/vendas
    alt crédito ok
        NEG->>ATV: POST /interno/ativos/{ticker}/estoque/devolucoes
        Note over NEG: ordem EXECUTADA
    else falha no crédito
        Note over NEG: transação: devolve as ações à carteira · ordem REJEITADA
    end
```

## 7. Depósito

```mermaid
sequenceDiagram
    autonumber
    participant APP as App
    participant PAG as pagamentos
    participant MQ as RabbitMQ
    participant CLI as clientes
    APP->>PAG: POST /api/pagamentos {valor, PIX | BOLETO | TED} (via gateway)
    Note over PAG: CobrancaPix / CobrancaBoleto / CobrancaTed gera as instruções
    PAG-->>APP: 201 · Pix copia e cola, linha digitável ou dados da TED
    loop a cada 2 s
        Note over PAG: conciliação: o provedor simulado confirma (Pix 3 s, TED 6 s, boleto 10 s)
    end
    PAG->>MQ: pagamento.confirmado
    MQ->>CLI: fila clientes.pagamentos-confirmados
    Note over CLI: transação: credita o depósito uma única vez por pagamentoId
```
