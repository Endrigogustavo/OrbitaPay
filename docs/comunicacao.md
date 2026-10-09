# Comunicação do ecossistema

Existem só três formas de comunicação no OrbitaPay:

| Forma | Quem usa | Como funciona |
|---|---|---|
| **HTTP** | App → Gateway → microserviço | O app só conhece o gateway. O gateway confere o acesso e repassa a requisição ao microserviço dono da rota. |
| **Evento (publica/assina)** | microserviço → RabbitMQ → microserviços | Quem publica envia para a exchange `orbita.eventos` com um tópico. Cada serviço interessado tem uma fila que assina aquele tópico. Quem publica não sabe quem vai receber. |
| **Consulta (request/reply)** | microserviço → RabbitMQ → microserviço | Quando um serviço precisa de uma resposta na hora, ele envia uma pergunta para uma fila e espera a resposta (*direct reply-to*). Também passa pelo RabbitMQ, nunca por HTTP. |

Os IPs e portas de cada container estão em [enderecos.md](enderecos.md). As filas e tópicos podem ser vistos ao vivo no painel http://localhost:8090.

## 1. Visão geral

A visão geral está dividida em três partes, uma para cada forma de comunicação.

### 1.1 HTTP: o app só fala com o gateway

```mermaid
flowchart LR
    APP["App mobile"] -->|"HTTP :8080"| GW["API Gateway<br/>confere o acesso"]
    GW -->|"/api/autenticacao"| AUT["Auth :8086"]
    GW -->|"/api/clientes"| CLI["Clientes :8081"]
    GW -->|"/api/contas"| CON["Contas :8082"]
    GW -->|"/api/ativos · /api/bolsas"| ATV["Ativos :8083"]
    GW -->|"/api/ordens · /api/ofertas"| NEG["Negociação :8084"]
    GW -->|"/api/carteiras"| CAR["Carteira :8085"]
    GW -->|"/api/pagamentos"| PAG["Pagamentos :8087"]
    GW -->|"/api/relatorios"| REL["Relatórios :8088"]
```

### 1.2 Eventos: quem publica, o tópico e quem consome

Leia da esquerda para a direita: o serviço da esquerda **publica** o tópico do meio na exchange `orbita.eventos`, e cada serviço da direita **recebe uma cópia** pela sua própria fila.

```mermaid
flowchart LR
    subgraph PUBLICA["Quem publica"]
        P_AUT["Auth"]
        P_CLI["Clientes"]
        P_ATV["Ativos"]
        P_NEG["Negociação"]
        P_CON["Contas"]
        P_CAR["Carteira"]
        P_PAG["Pagamentos"]
    end

    subgraph TOPICOS["Tópicos · exchange orbita.eventos"]
        T_CRED{{"credencial.*"}}
        T_CLI{{"cliente.*"}}
        T_ATV{{"ativo.*"}}
        T_ORD_C{{"ordem.compra-solicitada"}}
        T_ORD_V{{"ordem.venda-solicitada"}}
        T_ORD_E{{"ordem.executada"}}
        T_ORD_R{{"ordem.rejeitada"}}
        T_CON{{"conta.debito-*"}}
        T_CAR{{"carteira.acoes-*"}}
        T_PAG_C{{"pagamento.confirmado"}}
        T_PAG_E{{"pagamento.expirado"}}
    end

    subgraph CONSOME["Quem consome"]
        C_AUT["Auth"]
        C_CLI["Clientes"]
        C_CON["Contas"]
        C_NEG["Negociação"]
        C_CAR["Carteira"]
        C_REL["Relatórios"]
    end

    P_AUT --> T_CRED
    P_CLI --> T_CLI
    P_ATV --> T_ATV
    P_NEG --> T_ORD_C & T_ORD_V & T_ORD_E & T_ORD_R
    P_CON --> T_CON
    P_CAR --> T_CAR
    P_PAG --> T_PAG_C & T_PAG_E

    T_CRED --> C_CLI
    T_CLI --> C_AUT & C_CON & C_NEG & C_CAR & C_REL
    T_ATV --> C_NEG & C_CAR
    T_ORD_C --> C_CON
    T_ORD_V --> C_CAR
    T_ORD_E --> C_CON & C_CAR & C_REL
    T_ORD_R --> C_CAR & C_REL
    T_CON --> C_NEG
    T_CAR --> C_NEG
    T_PAG_C --> C_CON & C_REL
    T_PAG_E --> C_REL

    classDef topico fill:#fff4d6,stroke:#d9a400,color:#000
    class T_CRED,T_CLI,T_ATV,T_ORD_C,T_ORD_V,T_ORD_E,T_ORD_R,T_CON,T_CAR,T_PAG_C,T_PAG_E topico
```

### 1.3 Consultas: pergunta e resposta pelo RabbitMQ

```mermaid
flowchart LR
    NEG["Negociação"] -->|"1: pergunta pelo ticker"| Q_ATV[("fila ativos.consultas")]
    CAR["Carteira"] -->|"1: pergunta pelo ticker"| Q_ATV
    Q_ATV --> ATV["Ativos"]
    ATV -.->|"2: responde com os dados do ativo"| NEG & CAR

    CLI["Clientes"] -->|"1: pede a credencial (e-mail + PIN)"| Q_REG[("fila auth.registros-de-credencial")]
    Q_REG --> AUT["Auth"]
    AUT -.->|"2: responde: registrada ou recusada"| CLI
```

Cada microserviço também fala com **o seu próprio MongoDB** (`mongo-clientes`, `mongo-contas`…). Nenhum serviço acessa o banco de outro.

## 2. Diagramas de comunicação dos fluxos principais

Nos diagramas abaixo, cada seta tem um **número de ordem**, como no diagrama de comunicação da UML: siga os números para entender a sequência.

### 2.1 Cadastro de cliente

```mermaid
flowchart LR
    APP["App"] -->|"1: POST /api/clientes"| GW["Gateway"]
    GW -->|"2: repassa"| CLI["Clientes"]
    CLI -->|"3: pergunta: criar credencial (auth.registros-de-credencial)"| AUT["Auth"]
    AUT -.->|"4: responde: credencial criada"| CLI
    CLI -.->|"5: cliente.cadastrado"| CON["Contas<br/>abre a conta"]
    CLI -.->|"5: cliente.cadastrado"| NEG["Negociação<br/>registra o investidor"]
    CLI -.->|"5: cliente.cadastrado"| REL["Relatórios<br/>registra o fato"]
    CLI -->|"6: 201 Created"| APP
```

Se o Auth recusar o PIN (passo 4), Clientes desfaz o cadastro e nada é publicado.

### 2.2 Compra de ações (saga)

```mermaid
flowchart LR
    APP["App"] -->|"1: POST /api/ordens + X-Assinatura"| GW["Gateway"]
    GW -->|"2: repassa"| NEG["Negociação<br/>trava o ativo e reserva"]
    NEG -->|"3: 202 PENDENTE"| APP
    NEG -.->|"4: ordem.compra-solicitada"| CON["Contas<br/>trava a conta e debita"]
    CON -.->|"5: conta.debito-aprovado<br/>ou conta.debito-recusado"| NEG
    NEG -.->|"6: ordem.executada<br/>ou ordem.rejeitada"| CAR["Carteira<br/>registra a posição"]
    NEG -.->|"6: ordem.executada"| REL["Relatórios"]
    APP -->|"7: GET /api/ordens/{id} até sair de PENDENTE"| GW
```

Se o débito for recusado (passo 5), a Negociação **devolve a reserva** ao estoque e publica `ordem.rejeitada` (compensação da saga).

### 2.3 Venda de ações (saga)

```mermaid
flowchart LR
    APP["App"] -->|"1: POST /api/ordens (VENDA)"| GW["Gateway"]
    GW -->|"2: repassa"| NEG["Negociação"]
    NEG -.->|"3: ordem.venda-solicitada"| CAR["Carteira<br/>trava a carteira e reserva as ações"]
    CAR -.->|"4: carteira.acoes-reservadas<br/>ou carteira.acoes-insuficientes"| NEG
    NEG -.->|"5: ordem.executada"| CON["Contas<br/>credita o valor da venda"]
    NEG -.->|"5: ordem.executada"| CAR2["Carteira<br/>liquida a reserva"]
    NEG -.->|"5: ordem.executada"| REL["Relatórios"]
```

### 2.4 Depósito (Pagamentos + camada anticorrupção)

```mermaid
flowchart LR
    APP["App"] -->|"1: POST /api/pagamentos {PIX}"| GW["Gateway"]
    GW -->|"2: repassa"| PAG["Pagamentos"]
    PAG -->|"3: emite a cobrança pela fachada (ACL)"| PSP["Provedor Pix simulado"]
    PAG -->|"4: 201 com o Pix copia e cola"| APP
    PAG -->|"5: a cada 2 s consulta o provedor"| PSP
    PAG -.->|"6: pagamento.confirmado"| CON["Contas<br/>trava a conta e credita"]
    PAG -.->|"6: pagamento.confirmado"| REL["Relatórios"]
```

## 3. Todos os eventos

| Tópico | Publica | Consome | Para quê |
|---|---|---|---|
| `credencial.bloqueada-por-pin` | Auth | Clientes | bloquear o cliente após 3 PINs errados |
| `cliente.cadastrado` | Clientes | Contas, Negociação, Relatórios | abrir a conta, registrar o investidor e o fato |
| `cliente.atualizado` | Clientes | Auth, Contas, Relatórios | atualizar e-mail e nome |
| `cliente.situacao-alterada` | Clientes | Auth, Contas, Negociação, Relatórios | bloquear/desbloquear em todos os contextos |
| `cliente.removido` | Clientes | Auth, Contas, Negociação, Carteira, Relatórios | encerrar tudo do cliente |
| `ativo.listado` · `ativo.atualizado` | Ativos | Negociação, Carteira | manter a cópia local do ativo |
| `ativo.removido` | Ativos | Negociação, Carteira | tirar o ativo de negociação |
| `ativo.cotacoes-atualizadas` | Ativos (a cada 3 s) | Negociação, Carteira | atualizar as cotações |
| `ordem.compra-solicitada` | Negociação | Contas | debitar o valor da compra |
| `ordem.venda-solicitada` | Negociação | Carteira | reservar as ações vendidas |
| `ordem.executada` | Negociação | Contas, Carteira, Relatórios | creditar a venda, registrar a posição, contabilizar |
| `ordem.rejeitada` | Negociação | Carteira, Relatórios | devolver a reserva de venda, contabilizar |
| `conta.debito-aprovado` · `conta.debito-recusado` | Contas | Negociação | continuar ou compensar a saga de compra |
| `carteira.acoes-reservadas` · `carteira.acoes-insuficientes` | Carteira | Negociação | continuar ou cancelar a saga de venda |
| `pagamento.confirmado` | Pagamentos | Contas, Relatórios | creditar o depósito, contabilizar |
| `pagamento.expirado` | Pagamentos | Relatórios | contabilizar a cobrança expirada |

| Consulta (request/reply) | Pergunta | Responde | Para quê |
|---|---|---|---|
| `ativos.consultas` | Negociação, Carteira | Ativos | dados de um ativo que ainda não está na cópia local |
| `auth.registros-de-credencial` | Clientes | Auth | criar a credencial (PIN) no cadastro |

Se o consumo de uma mensagem falhar 3 vezes, ela vai para a exchange `orbita.eventos.mortos` e cai na **DLQ** do serviço (`contas.dlq`, `negociacao.dlq`…), em vez de ficar em loop.
