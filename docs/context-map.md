# Context Map — OrbitaPay

Este documento descreve os **bounded contexts** do OrbitaPay, como eles se relacionam e qual linguagem cada um publica. Cada contexto é um microserviço independente: processo próprio, porta própria, banco MongoDB próprio e modelo de domínio próprio. Os serviços não compartilham código nem banco. Entre si, eles só conversam por **mensageria (RabbitMQ)**. O **gateway** é a única porta de entrada do front-end e apenas delega as requisições HTTP. Ele nunca é usado para comunicação entre serviços.

## 1. Visão geral

```mermaid
flowchart LR
    APP["App React Native<br/>(Expo)"] -->|"HTTP /api/**"| GW["API Gateway<br/>:8080"]

    GW -->|"/api/autenticacao"| AUT
    GW -->|"/api/clientes"| CLI
    GW -->|"/api/contas"| CON
    GW -->|"/api/ativos, /api/bolsas"| ATV
    GW -->|"/api/ordens, /api/ofertas"| NEG
    GW -->|"/api/carteiras"| CAR
    GW -->|"/api/pagamentos"| PAG
    GW -->|"/api/relatorios"| REL

    subgraph AUT["Auth :8086"]
        AUTdb[(orbita_auth)]
    end
    subgraph CLI["Clientes :8081"]
        CLIdb[(orbita_clientes)]
    end
    subgraph CON["Contas :8082"]
        CONdb[(orbita_contas)]
    end
    subgraph ATV["Ativos :8083"]
        ATVdb[(orbita_ativos)]
    end
    subgraph NEG["Negociação :8084"]
        NEGdb[(orbita_negociacao)]
    end
    subgraph CAR["Carteira :8085"]
        CARdb[(orbita_carteira)]
    end
    subgraph PAG["Pagamentos :8087"]
        PAGdb[(orbita_pagamentos)]
    end
    subgraph REL["Relatórios :8088"]
        RELdb[(orbita_relatorios)]
    end

    MQ{{"RabbitMQ<br/>exchange orbita.eventos"}}
    AUT -.->|"credencial.*"| MQ
    CLI -.->|"cliente.*"| MQ
    ATV -.->|"ativo.*"| MQ
    NEG -.->|"ordem.*"| MQ
    CON -.->|"conta.*"| MQ
    CAR -.->|"carteira.*"| MQ
    PAG -.->|"pagamento.*"| MQ
    MQ -.->|"assinaturas"| AUT & CLI & CON & NEG & CAR & REL
```

## 2. Mapa de relacionamentos

```mermaid
flowchart TB
    AUT["<b>Auth</b><br/>(Credenciais e Tokens)<br/>OHS"]
    CLI["<b>Clientes</b><br/>(Cadastro e Situação)<br/>U · OHS/PL"]
    ATV["<b>Ativos</b><br/>(Catálogo e Cotações)<br/>U · OHS/PL"]
    NEG["<b>Negociação</b><br/>(Ordens e Oferta)<br/>Orquestrador da saga"]
    CON["<b>Contas</b><br/>(Saldo e Extrato)"]
    CAR["<b>Carteira</b><br/>(Custódia de ações)"]
    PAG["<b>Pagamentos</b><br/>(Depósitos)<br/>U · OHS/PL · ACL p/ provedores"]
    REL["<b>Relatórios</b><br/>(Modelo de leitura)"]

    CLI <-->|"Parceria · request/reply auth.registros-de-credencial ⇄ credencial.bloqueada-por-pin"| AUT
    CLI -->|"U → D · eventos cliente.* · ACL: Titular"| CON
    CLI -->|"U → D · eventos cliente.* · ACL: Investidor"| NEG
    CLI -->|"U → D · cliente.removido"| CAR
    CLI -->|"U → D · eventos cliente.* · ACL: Fato"| REL
    ATV -->|"U → D · eventos ativo.* + consulta request/reply · ACL: AtivoNegociavel"| NEG
    ATV -->|"U → D · eventos ativo.* + consulta request/reply · ACL: AtivoCotado"| CAR
    NEG <-->|"Parceria via saga · ordem.compra-solicitada ⇄ conta.debito-*"| CON
    NEG <-->|"Parceria via saga · ordem.venda-solicitada ⇄ carteira.acoes-*"| CAR
    NEG -->|"U → D · ordem.executada / ordem.rejeitada · ACL: Fato"| REL
    PAG -->|"U → D · pagamento.confirmado"| CON
    PAG -->|"U → D · pagamento.* · ACL: Fato"| REL
```

Legenda: **U** = upstream, **D** = downstream, **OHS** = Open Host Service, **PL** = Published Language, **ACL** = Anti-Corruption Layer.

| Upstream | Downstream | Padrão | Como o downstream se protege |
|---|---|---|---|
| Clientes | Auth | Parceria: request/reply na fila `auth.registros-de-credencial` (cadastro) + `cliente.*` (e-mail, desbloqueio, remoção) | O Auth guarda só `clienteId`, e-mail, hash do PIN e tentativas |
| Auth | Clientes | OHS + PL (`credencial.bloqueada-por-pin`) | Clientes bloqueia a conta com motivo `PIN` |
| Clientes | Contas | OHS + PL (`cliente.*`) | ACL: o listener traduz a mensagem para `Titular` (nome e se está bloqueado) dentro da `Conta` |
| Clientes | Negociação | OHS + PL (`cliente.*`) | ACL: projeção `Investidor` (só `clienteId` e `bloqueado`) |
| Clientes | Carteira | OHS + PL (`cliente.removido`) | Só reage ao encerramento de cadastro |
| Ativos | Negociação | OHS + PL (`ativo.*`) + consulta síncrona por mensageria (`ativos.consultas`) | ACL: projeção `AtivoNegociavel` com dados próprios da negociação (ações disponíveis e reservadas) |
| Ativos | Carteira | OHS + PL (`ativo.*`) + consulta por mensageria | ACL: projeção `AtivoCotado` (cotação e câmbio para valorizar posições) |
| Negociação | Contas | Parceria (saga orquestrada pela Negociação) | Contrato de mensagens da saga; idempotência por `ordemId` |
| Negociação | Carteira | Parceria (saga orquestrada pela Negociação) | Contrato de mensagens da saga; idempotência por `ordemId` |
| Pagamentos | Contas | OHS + PL (`pagamento.confirmado`) | Crédito do depósito sob trava, uma única vez por `pagamentoId` |
| Clientes, Negociação, Pagamentos | Relatórios | OHS + PL (`cliente.#`, `ordem.executada`, `ordem.rejeitada`, `pagamento.#`) | ACL: `EventosListener` traduz cada mensagem em um `Fato`, idempotente por `eventoId` |

## 3. O mesmo conceito, modelos diferentes

O "ativo" (ação) existe em três contextos, e cada um guarda só o que precisa. É o equivalente ao exemplo em que o contexto de **Produto** é dono do cadastro completo e o contexto de **Venda** mantém o próprio `Produto`, reduzido ao que interessa para vender.

| Contexto | Classe | O que guarda | Quem é dono |
|---|---|---|---|
| Ativos | `Ativo` | ticker, empresa, setor, bolsa, cotação, histórico de 40 pontos, quantidade emitida | **Dono do dado** |
| Negociação | `AtivoNegociavel` | ticker, cotação, moeda, câmbio, quantidade emitida, **disponível**, **reservada**, negociável | Projeção + estado próprio (livro de oferta) |
| Carteira | `AtivoCotado` | ticker, nome, moeda, câmbio, cotação | Projeção somente-leitura |

O mesmo vale para o cliente:

| Contexto | Classe | O que guarda |
|---|---|---|
| Clientes | `Cliente` | nome, e-mail, CPF, bloqueio e motivo do bloqueio |
| Auth | `Credencial` | `clienteId`, e-mail de login, PIN (hash PBKDF2) e tentativas erradas |
| Contas | `Conta.nomeTitular` / `titularBloqueado` | só o necessário para exibir o extrato e barrar saques |
| Negociação | `Investidor` | só `clienteId` e `bloqueado` |
| Relatórios | `PerfilDeCliente` | `clienteId`, nome, bloqueado, ativo e data de cadastro, para os rankings e a contagem da base |

Quando um contexto precisa de um dado que ainda não tem, ele **pergunta ao dono por mensageria** (request/reply na fila `ativos.consultas`), sem chamada HTTP entre serviços. Exemplos:
- a Negociação recebe uma ordem de um ticker que ainda não está na sua projeção;
- a Carteira valoriza uma posição de um ativo cuja cotação ainda não conhece.

## 4. Linguagem publicada (eventos)

Exchange: `orbita.eventos` (topic, durável). Toda mensagem carrega `eventoId`, `evento`, `mensagem` (texto legível) e `enviadoEm`, no mesmo formato dos DTOs de mensageria do projeto de referência.

| Routing key | Publicado por | Consumido por | Conteúdo principal |
|---|---|---|---|
| `credencial.bloqueada-por-pin` | Auth | Clientes | clienteId |
| `cliente.cadastrado` | Clientes | Contas, Negociação, Relatórios | clienteId, nome, email, depositoInicial |
| `cliente.atualizado` | Clientes | Auth, Contas, Relatórios | clienteId, nome, email |
| `cliente.situacao-alterada` | Clientes | Auth, Contas, Negociação, Relatórios | clienteId, bloqueado, motivo |
| `cliente.removido` | Clientes | Auth, Contas, Negociação, Carteira, Relatórios | clienteId |
| `ativo.listado` / `ativo.atualizado` | Ativos | Negociação, Carteira | ticker, nome, setor, bolsa, moeda, câmbio, cotação, quantidadeEmitida |
| `ativo.removido` | Ativos | Negociação, Carteira | ticker |
| `ativo.cotacoes-atualizadas` | Ativos (a cada 3 s) | Negociação, Carteira | lista de (ticker, cotação) |
| `ordem.compra-solicitada` | Negociação | Contas | ordemId, clienteId, ticker, quantidade, valorTotal |
| `ordem.venda-solicitada` | Negociação | Carteira | ordemId, clienteId, ticker, quantidade |
| `ordem.executada` | Negociação | Contas (crédito da venda), Carteira (liquidação), Relatórios | ordem completa |
| `ordem.rejeitada` | Negociação | Carteira (libera reserva de venda), Relatórios | ordem completa + motivo |
| `conta.debito-aprovado` / `conta.debito-recusado` | Contas | Negociação | ordemId, valor ou motivo |
| `carteira.acoes-reservadas` / `carteira.acoes-insuficientes` | Carteira | Negociação | ordemId, quantidade ou motivo |
| `pagamento.confirmado` | Pagamentos | Contas (crédito do depósito), Relatórios | pagamentoId, clienteId, método, valor, valorPago, provedor, concluidoEm |
| `pagamento.expirado` | Pagamentos | Relatórios | pagamentoId, clienteId, método, valor |

Consultas síncronas por mensageria (request/reply via *direct reply-to*):
- `ativos.consultas` recebe `{ticker, solicitante}` e responde `{encontrado, ticker, nome, bolsa, moeda, cambio, cotacao, quantidadeEmitida}`;
- `auth.registros-de-credencial` recebe `{clienteId, email, pin}` no cadastro do cliente e responde `{registrada, motivo}`.

### Filas e ordenação

- **Eventos de ciclo de vida** (`cliente.#` e `ativo.#`) caem em **uma fila por contexto consumidor**, com consumidor único. Isso garante que "cadastrado → bloqueado" chegue na ordem certa.
- **Mensagens da saga** têm uma fila por tipo, com consumo concorrente, e a trava pessimista protege os agregados.
- Toda fila tem *dead-letter* para `orbita.eventos.mortos`, que encaminha para `<servico>.dlq`. Uma mensagem que falha 3 vezes (retry com backoff) vai para a DLQ em vez de entrar em loop.

## 5. Saga de compra de ações

```mermaid
sequenceDiagram
    autonumber
    participant APP as App
    participant GW as Gateway
    participant AUT as Auth
    participant NEG as Negociação
    participant CON as Contas
    participant CAR as Carteira

    APP->>GW: POST /api/autenticacao/assinaturas {pin}
    GW->>AUT: (sessão validada, X-Cliente-Id injetado)
    AUT-->>APP: token de assinatura (120 s)
    APP->>GW: POST /api/ordens {ticker, COMPRA, qtd} + X-Assinatura
    GW->>NEG: repassa com X-Cliente-Id
    Note over NEG: LOCK PESSIMISTA no AtivoNegociavel<br/>disponível -= qtd · reservada += qtd
    NEG-->>APP: 202 Accepted · ordem PENDENTE
    NEG--)CON: ordem.compra-solicitada
    Note over CON: LOCK PESSIMISTA na Conta<br/>valida bloqueio e saldo · debita
    alt saldo suficiente
        CON--)NEG: conta.debito-aprovado
        Note over NEG: LOCK no ativo · reservada -= qtd<br/>ordem EXECUTADA
        NEG--)CAR: ordem.executada
        Note over CAR: LOCK na Carteira<br/>posição += qtd · preço médio
    else saldo insuficiente / conta bloqueada
        CON--)NEG: conta.debito-recusado
        Note over NEG: LOCK no ativo · devolve a reserva<br/>ordem REJEITADA (compensação)
    end
    loop até sair de PENDENTE
        APP->>GW: GET /api/ordens/{id}
    end
```

A **venda** é simétrica:
1. A Negociação cria a ordem e publica `ordem.venda-solicitada`.
2. A Carteira trava a carteira do cliente e **reserva** as ações. Ela responde `carteira.acoes-reservadas` ou `carteira.acoes-insuficientes`.
3. Com a reserva confirmada, a Negociação trava o ativo, devolve as ações ao mercado e executa a ordem.
4. Em seguida publica `ordem.executada`: a Carteira liquida a reserva e a Conta credita o valor.

## 6. Lock pessimista no MongoDB

O projeto de referência usa `@Lock(PESSIMISTIC_WRITE)` com `SELECT ... FOR UPDATE` no H2. O MongoDB não tem esse recurso, então cada serviço que altera saldo ou quantidade implementa a trava explicitamente em `persistence/trava/TravaPessimistaMongo`, no módulo `springframework`:

1. **Adquirir:** um `findAndModify` atômico só modifica o documento se `trava` for nulo ou estiver expirado, e grava `trava = {dono: UUID, adquiridaEm, expiraEm}`. Se outra operação estiver com a trava, a thread espera com *backoff* e tenta de novo até `orbita.trava.espera-maxima-ms` (10 s, equivalente ao `LOCK_TIMEOUT=10000` do exemplo). Passado esse tempo, lança `TravaIndisponivelException` (HTTP 409).
2. **Salvar e liberar:** um `findAndReplace` filtrado por `trava.dono == UUID` grava o novo estado com `trava = null`. Salvar e liberar acontece em **uma única operação atômica**. Se a trava tiver expirado e outro processo tiver assumido, a gravação é recusada, o que evita *lost update*.
3. **Liberar sem alterar:** se a regra de negócio falhar (saldo ou oferta insuficiente), um `$unset` remove a trava.
4. **Expiração** (`expiracao-ms`, 30 s): evita *deadlock* se uma instância cair segurando a trava.

Como a trava fica **no banco**, ela funciona entre **várias instâncias** do mesmo serviço, que é o cenário do teste de concorrência do projeto de referência (duas instâncias disputando o mesmo estoque). A camada de aplicação usa a trava pelo caso de uso `OperacaoComTrava`, que chama `travar…`, `salvarELiberar` e `liberar` da interface do repositório (`ContaRepository`, `AtivoNegociavelRepository`, `CarteiraRepository`), e não sabe que existe MongoDB por trás.

Agregados protegidos:

| Serviço | Documento travado | Operações |
|---|---|---|
| Contas | `contas` (por clienteId) | crédito de depósito confirmado, saque, débito de compra, crédito de venda |
| Negociação | `ativos_negociaveis` (por ticker) | reserva de compra, confirmação, compensação, devolução de venda |
| Carteira | `carteiras` (por clienteId) | reserva de venda, liquidação de compra e venda, cancelamento |

## 7. Clean Architecture em cada serviço

Cada serviço é separado como um **monolito modular**, em dois módulos Maven, publicados como `<contexto>-micro-domain` e `<contexto>-micro-springframework` (o agregador é `<contexto>-micro`). O pacote Java continua `com.orbitapay.<contexto>`. O que muda é o módulo em que cada camada fica. Os casos de uso são classes concretas, chamadas diretamente pelos controllers e listeners, sem interfaces de entrada:

```
<servico>/
├── pom.xml                  agregador (sem parent do Spring)
├── domain/                  NÚCLEO: pom sem nenhuma dependência
│   └── com.orbitapay.<contexto>
│       ├── domain
│       │   ├── model        entidades e value objects
│       │   ├── event        eventos de domínio
│       │   ├── exception
│       │   └── repository   interfaces dos repositórios
│       └── application
│           ├── usecase      casos de uso (classes puras, sem anotações)
│           ├── service      interfaces de serviços técnicos: publicador, catálogo, codificador…
│           └── dto
└── springframework/         Spring Boot: depende de domain + starters
    └── com.orbitapay.<contexto>
        ├── <Contexto>Application
        ├── web              controllers REST e DTOs HTTP
        ├── messaging        listeners (ACL de entrada), publicadores, request/reply e mensagens
        ├── persistence      documentos Mongo, repositórios Spring Data, trava pessimista
        └── infrastructure   @Configuration que monta os casos de uso, topologia Rabbit, seed
```

Alguns contextos têm pacotes técnicos próprios no `springframework`: `seguranca` (Auth e gateway), `acl` e `provedor` (Pagamentos), `mercado` e `agendamento` (Ativos), `agendamento` (Pagamentos).

As dependências sempre apontam para dentro: `infrastructure → web/messaging/persistence → application → domain`. O módulo `domain` não tem Spring no classpath. Por isso, a regra "nada do Spring no núcleo" é garantida pelo compilador e não só por convenção. Os casos de uso são instanciados em `CasosDeUsoConfig`, no módulo `springframework`.
