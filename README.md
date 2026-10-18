# OrbitaPay

Banco e corretora de ações em **microserviços**. Cada serviço tem o seu próprio banco MongoDB e segue a **Clean Architecture**. O app **React Native (Expo)** fala somente com o **gateway**.

- Comunicação entre os serviços (HTTP e RabbitMQ), com diagramas: [docs/comunicacao.md](docs/comunicacao.md)
- Diagramas UML em PlantUML: [docs/plantuml/](docs/plantuml)
- Teste de concorrência com k6: [k6/concorrencia.js](k6/concorrencia.js)

## Serviços

| Serviço | Porta | Banco MongoDB | Responsabilidade |
|---|---|---|---|
| `gateway` | 9000 | — | Porta de entrada do app. Roteia, confere a sessão e a assinatura no `auth` e injeta a identidade (`X-Cliente-Id`, `X-Perfil`) |
| `auth` | 9001 | `orbita_auth` | Credenciais (e-mail + PIN), equipe (gerente e admin), sessões por cookie e assinaturas com PIN |
| `clientes` | 9002 | `orbita_clientes` | Cadastro, bloqueio e desbloqueio do cliente **e a conta**: saldo, extrato, saque, débito/crédito das ordens e crédito dos depósitos |
| `ativos` | 9003 | `orbita_ativos` | Catálogo de ações, bolsas, cotações simuladas e o **estoque de ações** de cada ativo |
| `negociacao` | 9004 | `orbita_negociacao` | Ordens de compra e venda e a **carteira** do cliente (posições e preço médio) |
| `pagamentos` | 9005 | `orbita_pagamentos` | Depósitos por Pix, boleto e TED (provedores simulados) |

Infraestrutura: **RabbitMQ** (5672, painel em http://localhost:15672, usuário e senha `orbita`) e **um MongoDB por serviço**, em replica set para permitir transações.

## Como os serviços conversam

- **HTTP** quando um serviço precisa de uma resposta na hora. As rotas `/interno/...` só existem entre serviços: o gateway não as expõe.
- **RabbitMQ** (exchange `orbita.eventos`) só onde faz sentido ser assíncrono:

| Evento | Publica | Consome | Por quê |
|---|---|---|---|
| `ordem.solicitada` | negociacao | negociacao | a ordem é aceita na hora (`202`) e executada em segundo plano, uma por vez, sem prender a requisição do app |
| `pagamento.confirmado` | pagamentos | clientes | o crédito acontece em segundo plano e não pode se perder se `clientes` estiver fora do ar |
| `cliente.removido` | clientes | auth, negociacao, pagamentos | vários serviços limpam os dados do cliente sem que `clientes` precise conhecer cada um |

Cada entidade guarda só o que é dela. Quando precisa de outro dado, busca pelo ID no serviço dono: a negociação pede a cotação do ativo pelo `ticker` ao `ativos` e a situação do cliente pelo `clienteId` ao `clientes`.

## Estrutura de cada serviço (Clean Architecture)

Cada pasta em `backend/` é um projeto Maven com dois módulos, no padrão do projeto [introducao](https://github.com/FATEC-Multiplataforma2026/introducao):

```
backend/<servico>/
├── domain/                     REGRAS DE NEGÓCIO (da empresa) · Java puro, sem Spring
│   ├── entity/                 records com os dados de cada entidade
│   ├── service/                regras da empresa: saldo suficiente, limite de saque, preço médio, CPF válido…
│   └── exception/              erros de regra de negócio
└── spring/                     REGRAS DA APLICAÇÃO + frameworks
    ├── application/
    │   ├── usecase/            o que a aplicação faz: orquestra serviços de domínio, gateways e transações
    │   ├── gateway/            interfaces que os casos de uso usam (banco, outros serviços, eventos), sempre com prefixo I (ex.: IAtivoGateway)
    │   └── exception/          erros da aplicação (não encontrado, PIN incorreto, sessão inválida…)
    ├── adapter/
    │   ├── controller/         REST: controllers, dto/request, dto/response e handler/ (erros → HTTP)
    │   ├── gateway/
    │   │   ├── http/           chamadas a outros serviços (GatewayHttpBase + implementações, dto/)
    │   │   ├── mongo/          implementações com MongoDB (document/, repository/)
    │   │   └── exception/      falha de comunicação com outro serviço
    │   ├── messaging/          RabbitMQ: listener/, publisher/, dto/ e CanaisDeMensageria (nomes das filas)
    │   ├── interfaces/         contratos que o adapter precisa da infraestrutura (ex.: ICookieDeSessao)
    │   └── scheduler/          tarefas agendadas
    └── infrastructure/
        ├── config/             monta os casos de uso (UseCaseConfig) e os clientes HTTP
        ├── messaging/          declara exchange, filas e o conversor JSON do RabbitMQ
        ├── security/           cookie de sessão, PIN (PBKDF2), hash do token, filtro de acesso do gateway
        ├── transaction/        transação do MongoDB com nova tentativa em caso de conflito
        └── seed/               dados de demonstração
```

**Regra de negócio × regra da aplicação.** O que vale para a empresa em qualquer sistema fica no `domain/service` (ex.: "não sacar mais que o saldo", "limite de R$ 5.000 por saque", "preço médio da posição", "valor mínimo do boleto"). O que só existe nesta aplicação fica em `spring/application/usecase` (ex.: "bloquear o cliente depois de 3 PINs errados", "sessão de 4 h em cookie", "assinatura de 120 s", "desfazer o cadastro em até 10 minutos", compensações entre serviços).

**Regra de dependência.** As setas sempre apontam para dentro: `infrastructure → adapter → application → domain`. O `domain` não tem nenhuma dependência no `pom.xml`. O `application` não importa nada de `adapter` nem de `infrastructure`. Quando o adapter precisa de algo técnico (cookie, hash), ele declara uma interface em `adapter/interfaces` e a `infrastructure` implementa. Toda interface usada para inversão de dependência começa com `I` (`IAtivoGateway`, `ICookieDeSessao`); a implementação diz a tecnologia (`AtivoHttpGateway`, `CookieDeSessaoHttpOnly`).

**Padrões de projeto usados**

| Padrão | Onde |
|---|---|
| Template Method | `ExecucaoDeOrdem` (abstrata) com `ExecucaoDeCompra` e `ExecucaoDeVenda`: o esqueleto "movimentar dinheiro e ações → registrar na carteira → executar ou rejeitar" é o mesmo; cada tipo implementa as etapas |
| Strategy + Template Method | `CobrancaSimulada` (abstrata) com `CobrancaPix`, `CobrancaBoleto` e `CobrancaTed`; o `ProvedorDePagamentoSimuladoGateway` escolhe a estratégia pelo método |
| Classe abstrata base | `GatewayHttpBase`: toda chamada HTTP entre serviços passa por ela, que traduz `4xx` em recusa de negócio e falhas de rede em `503` |
| Adapter / Gateway | cada interface de `application/gateway` tem uma implementação em `adapter/gateway` (Mongo, HTTP ou RabbitMQ) |
| Injeção de dependência | os casos de uso são classes Java puras montadas em `UseCaseConfig` |

## Transação e concorrência

- **Transação do MongoDB** (`infrastructure/transaction/TransacaoMongoGateway`): o cadastro grava cliente e conta juntos; cada movimentação da conta e da carteira e o registro da ordem executada acontecem dentro de uma transação. Se duas transações alteram o mesmo documento ao mesmo tempo, o MongoDB acusa conflito e a operação inteira é repetida, sem perder atualização.
- **Estoque de ações** (`ativos`): a retirada é uma atualização atômica que só acontece se houver quantidade disponível (`quantidadeDisponivel >= pedido`), então o estoque nunca fica negativo.
- **Idempotência**: cada lançamento da conta guarda a referência da operação (ordem ou pagamento), então repetir a mesma mensagem não credita nem debita duas vezes.

## Compra e venda de ações

**Compra** (`POST /api/ordens` com `X-Assinatura`):

1. A negociação busca a cotação no `ativos` e a situação do cliente no `clientes` (HTTP), grava a ordem `PENDENTE`, publica `ordem.solicitada` no RabbitMQ e responde `202`.
2. O `OrdemSolicitadaListener` consome a fila `negociacao.ordens-solicitadas` e retira as ações do **estoque** do ativo (HTTP para `ativos`).
3. Debita a conta (HTTP para `clientes`). Se faltar saldo, devolve as ações ao estoque e a ordem fica `REJEITADA`.
4. Em uma transação: soma as ações na carteira (preço médio) e marca a ordem como `EXECUTADA`.

**Venda**: retira as ações da carteira, credita a conta e devolve as ações ao estoque do ativo. Se o crédito falhar, as ações voltam para a carteira.

O app consulta `GET /api/ordens/{id}` até a ordem sair de `PENDENTE` para `EXECUTADA` ou `REJEITADA` (com o motivo). Se a mensagem chegar duas vezes, só a primeira executa, porque a ordem já não está mais pendente. O ativo `ORBT3` foi emitido com apenas **10 ações** para demonstrar o estoque.

## Autenticação

- **Login**: `POST /api/autenticacao/sessoes` grava a sessão no cookie `orbita_sessao` (`HttpOnly`, `SameSite=Lax`). O `auth` guarda só o hash do token, com expiração automática.
- **Gateway**: em toda requisição com cookie, pergunta ao `auth` (`GET /interno/acessos`) quem é o usuário e se a assinatura é válida. Sair, ser removido ou virar gerente derruba a sessão na hora.
- **Assinatura**: `POST /api/autenticacao/assinaturas` confere o PIN e devolve um token de 120 s, exigido em saques, ordens, desbloqueio e "tornar-se gerente".
- **Bloqueio por PIN**: na 3ª tentativa errada o `auth` pede ao `clientes` (HTTP) para bloquear o cliente com motivo `PIN`.

| Perfil | Quem é | O que pode |
|---|---|---|
| `CLIENTE` | quem abre conta | a própria conta: saldo, depósito, saque, ordens, carteira, PIN |
| `GERENTE` | funcionário | listar, cadastrar, editar, bloquear e desbloquear clientes; ver contas e carteiras |
| `ADMIN` | administrador | tudo do gerente + excluir clientes e cadastrar, editar e remover ações |

## Como rodar

### Tudo em containers

Requisito: Docker Desktop.

```bash
docker compose up --build -d
```

Sobe o RabbitMQ, um MongoDB por serviço e os 6 serviços na rede `orbita`. Ficam expostos: **9000** (gateway, usado pelo app), **15672** (painel do RabbitMQ) e **5672** (AMQP).

> Vindo da versão anterior (9 serviços)? Apague os dados antigos antes: `docker compose down -v --remove-orphans`.

### Cada serviço na sua máquina: `scripts/orbita.sh`

Requisitos: Docker Desktop, Java 21, Maven 3.9 e Node.js 20. No Windows, rode pelo Git Bash.

```bash
scripts/orbita.sh subir            # RabbitMQ e MongoDB no Docker, serviços com mvn spring-boot:run e o app web
scripts/orbita.sh subir --limpar   # o mesmo, apagando os dados (volta aos dados de demonstração)
scripts/orbita.sh status           # o que está no ar
scripts/orbita.sh logs clientes    # log de um serviço (sem nome: todos)
scripts/orbita.sh parar            # desliga serviços, app e containers (os dados ficam)
```

Para rodar um serviço sozinho (pelo VS Code ou pelo terminal):

```bash
docker compose --profile local up -d rabbitmq mongo-local      # infraestrutura
mvn -f backend/<servico>/pom.xml install -DskipTests           # instala o módulo domain, do qual o spring depende
mvn -f backend/<servico>/spring/pom.xml spring-boot:run
```

As variáveis `MONGODB_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`, `AUTH_URL`, `CLIENTES_URL`, `ATIVOS_URL`, `NEGOCIACAO_URL`, `PAGAMENTOS_URL`, `ORBITA_COOKIE_SEGURO` (use `true` atrás de HTTPS) e `ORBITA_DADOS_INICIAIS` (use `false` para não criar os dados de demonstração) sobrescrevem os padrões.

### App mobile

```bash
cd mobile
npm install
npm run web         # abre o app no navegador em http://localhost:19006
npm run typecheck
```

O app aponta para `http://localhost:9000` (ou `http://10.0.2.2:9000` no emulador Android). Em um celular físico: `EXPO_PUBLIC_API_URL=http://<ip-da-maquina>:9000 npx expo start`. No Android o app roda em *development build* (`npx expo run:android`), não no Expo Go.

Contas de demonstração:

| E-mail | PIN | Observação |
|---|---|---|
| ana@orbita.com | 1234 | saldo R$ 12.480,55 |
| bruno@orbita.com | 4321 | saldo R$ 3.150,00 |
| carla@orbita.com | 1111 | bloqueada por PIN |
| gerente@orbita.com | 0000 | gerente |
| admin@orbita.com | 9999 | admin |

## Teste de concorrência (k6)

```bash
k6 run k6/concorrencia.js                          # com o k6 instalado e o gateway em localhost:9000
docker compose run --rm k6 run concorrencia.js     # pelo container, com o stack do docker compose no ar
```

Com o `scripts/orbita.sh` use o k6 instalado: o container do compose depende do gateway do compose, que disputa a porta 9000 com o gateway local. Se o PIN do cliente mudou, informe com `-e PIN=...`; com o PIN errado o teste para logo no início com a mensagem do login (e três tentativas erradas bloqueiam o cliente, como no app).

O [`concorrencia.js`](k6/concorrencia.js) entra como o Bruno, lê o saldo e a cotação de `PETR4` em `GET /api/ativos/PETR4` e dispara **50 compras simultâneas** que, juntas, custam cerca de 4 vezes o saldo. Cada ordem cabe no saldo sozinha, mas não todas juntas. No fim, confere pelo extrato:

- nenhuma compra passou do saldo e o saldo nunca ficou negativo;
- nenhuma ordem foi debitada duas vezes;
- o saldo final é o inicial menos o que foi debitado;
- o estoque do ativo caiu exatamente o que foi executado.

Resultado esperado: algumas ordens `EXECUTADA` (o que o saldo paga), as demais `REJEITADA` com "Saldo insuficiente…", e as métricas `erro_inesperado` e `saldo_inconsistente` em zero. As variáveis `BASE_URL`, `EMAIL`, `PIN`, `TICKER`, `QTD_REQUISICOES`, `VEZES_O_SALDO` e `QUANTIDADE` mudam os padrões.

## Endpoints do gateway

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/api/autenticacao/sessoes` (login) · GET/DELETE `/api/autenticacao/sessoes/atual` | público |
| POST | `/api/autenticacao/credenciais` (e-mail e PIN do novo cliente) | público (o gerente recebe só o `clienteId`) |
| POST | `/api/autenticacao/assinaturas` · PUT `/api/autenticacao/pin` · GET `/api/autenticacao/me` · DELETE `/api/autenticacao/credenciais/me` | cliente |
| POST | `/api/autenticacao/me/gerente` | cliente + `X-Assinatura` |
| DELETE | `/api/autenticacao/credenciais/{clienteId}` | gerente ou admin |
| POST | `/api/clientes` | qualquer sessão (gerente e admin informam `clienteId` e `depositoInicial`) |
| GET/PUT/DELETE | `/api/clientes/me` · POST `/api/clientes/me/bloqueio` | cliente |
| POST | `/api/clientes/me/desbloqueio` | cliente + `X-Assinatura` |
| GET/PUT | `/api/clientes`, `/api/clientes/{id}` · POST `/bloqueio`, `/desbloqueio` | gerente ou admin |
| DELETE | `/api/clientes/{id}` | admin |
| GET | `/api/contas/me` | cliente |
| POST | `/api/contas/me/saques` | cliente + `X-Assinatura` |
| GET | `/api/contas` | gerente ou admin |
| POST/GET | `/api/pagamentos` · GET `/api/pagamentos/{id}` | cliente |
| GET | `/api/ativos`, `/api/ativos/{ticker}` (com `quantidadeDisponivel`), `/api/bolsas` | público |
| POST/PUT/DELETE | `/api/ativos[/{ticker}]` | admin |
| POST | `/api/ordens` | cliente + `X-Assinatura` |
| GET | `/api/ordens`, `/api/ordens/{id}` | cliente |
| GET | `/api/carteiras/me` · `/api/carteiras/{clienteId}` | cliente · gerente ou admin |
