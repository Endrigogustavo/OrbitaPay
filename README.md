# OrbitaPay

Ecossistema de **microserviços** para o banco e a corretora Órbita. Cada serviço corresponde a um **bounded context (DDD)** e é organizado em **camadas** (domínio, aplicação e infraestrutura). A comunicação entre serviços é feita exclusivamente por **mensageria (RabbitMQ)**. O app **React Native (Expo)** fala somente com o **API Gateway**.

> **Primeira vez rodando o projeto?** Siga o guia passo a passo: [docs/como-rodar.md](docs/como-rodar.md).

- **Diagrama de comunicação** do ecossistema inteiro (HTTP, eventos e consultas): [docs/comunicacao.md](docs/comunicacao.md)
- **Endereços e IPs** de todos os containers: [docs/enderecos.md](docs/enderecos.md)
- **SOLID** no código, com exemplos: [docs/solid.md](docs/solid.md)
- **Transações e consistência** (onde há `@Transactional`, saga e idempotência): [docs/transacoes.md](docs/transacoes.md)
- **Painel do RabbitMQ** (exchanges, filas e tópicos): http://localhost:15672 (usuário e senha `orbita`)
- Documentação do Context Map, da saga, das camadas anticorrupção e do lock pessimista: [docs/context-map.md](docs/context-map.md)
- Por que o MongoDB não tem lock pessimista e como ele foi implementado: [docs/lock-pessimista.md](docs/lock-pessimista.md)
- Diagramas UML em PlantUML: [docs/plantuml/](docs/plantuml/README.md)
- Teste de concorrência com k6: [k6/concorrencia.js](k6/concorrencia.js)

## Serviços

| Serviço | IP | Porta | Banco MongoDB | Responsabilidade |
|---|---|---|---|---|
| `gateway` | `172.30.0.80` | 8080 | — | Entrada única do front. Roteia, valida sessão (HMAC), exige assinatura por PIN e injeta a identidade |
| `auth` | `172.30.0.86` | 8086 | `orbita_auth` | Credenciais (PIN), contas da equipe (gerente e admin), login único, tokens de sessão e de assinatura, bloqueio por tentativas |
| `clientes` | `172.30.0.81` | 8081 | `orbita_clientes` | Cadastro, dados pessoais e situação da conta (bloqueio e desbloqueio) |
| `contas` | `172.30.0.82` | 8082 | `orbita_contas` | Saldo, extrato, saque, crédito dos depósitos confirmados, débito e crédito das ordens |
| `ativos` | `172.30.0.83` | 8083 | `orbita_ativos` | Catálogo de ações, bolsas, cotações simuladas ao vivo |
| `negociacao` | `172.30.0.84` | 8084 | `orbita_negociacao` | Ordens de compra e venda (saga), oferta disponível por ativo |
| `carteira` | `172.30.0.85` | 8085 | `orbita_carteira` | Custódia: posições, preço médio, reservas de venda |
| `pagamentos` | `172.30.0.87` | 8087 | `orbita_pagamentos` | Depósitos por Pix, boleto e TED: fachada com camada anticorrupção sobre os provedores |
| `relatorios` | `172.30.0.88` | 8088 | `orbita_relatorios` | Relatório gerencial e extrato de investimentos, montados a partir dos eventos |

Todos ficam na rede Docker `orbita` (`172.30.0.0/24`) com IP fixo: o final do IP é a porta do serviço. A lista completa, com RabbitMQ, painel e bancos, está em [docs/enderecos.md](docs/enderecos.md).

Cada pasta em `backend/` é um projeto Maven **independente** (Spring Boot 4.0 / Java 21): `pom.xml`, `Dockerfile`, `application.yaml` e banco próprios. Não existe código nem banco compartilhado. Os microserviços usam o sufixo `-micro` no `artifactId` e no `spring.application.name` (`clientes-micro`, `contas-micro`…); a pasta e o serviço do compose levam só o nome do contexto (`clientes`, `contas`…).

### Camadas de cada serviço

Por dentro, cada serviço tem dois módulos Maven e segue o **SOLID** ([docs/solid.md](docs/solid.md)): cada caso de uso é uma **classe com uma única ação** (`Sacar`, `DebitarCompra`, `ConfirmarCompra`…), chamada diretamente pelos controllers e listeners, sem interfaces de "porta de entrada". Só continua sendo interface o que o núcleo em Java puro precisa que a infraestrutura implemente: os repositórios e alguns serviços técnicos, como publicar eventos ou codificar o PIN.

| Módulo | Pacotes | Dependências |
|---|---|---|
| `domain` | `domain.model` (entidades e value objects), `domain.event`, `domain.exception`, `domain.repository` (interfaces dos repositórios) · `application.usecase` (casos de uso), `application.service` (interfaces de serviços técnicos: publicador, catálogo, codificador…), `application.dto` | **nenhuma**: Java puro, sem Spring, MongoDB, RabbitMQ ou Jackson |
| `springframework` | `web` (controllers e DTOs HTTP), `messaging` (listeners, publicadores, mensagens), `persistence` (documentos Mongo e implementações dos repositórios), `infrastructure` (configuração, seed) | `domain` + starters do Spring |

O `pom.xml` na raiz de cada serviço só agrega os dois módulos e não herda do Spring, então nada do framework chega ao `domain`. Como o `domain` não declara dependências, qualquer `import org.springframework...` nele quebra a compilação.

```
OrbitaPay/
├── backend/
│   ├── gateway/
│   ├── auth/
│   ├── clientes/
│   │   ├── domain/              núcleo em Java puro (pom sem dependências)
│   │   ├── springframework/     Spring Boot: web, messaging, persistence e configuração
│   │   ├── pom.xml              agregador dos módulos
│   │   └── Dockerfile
│   ├── contas/
│   ├── ativos/
│   ├── negociacao/
│   ├── carteira/
│   ├── pagamentos/
│   └── relatorios/
├── mobile/                  app Expo / React Native em TypeScript (expo-router)
├── k6/concorrencia.js       teste de compra simultânea (lock pessimista)
├── scripts/orbita.sh        modo local: infraestrutura no Docker, serviços com mvn spring-boot:run e o app
├── scripts/rodar-tudo.sh    tudo em containers + todos os testes
├── docs/                    comunicação, endereços, SOLID, context map, lock pessimista e PlantUML
└── docker-compose.yml
```

## Como rodar

O passo a passo completo (pré-requisitos, testes pela linha de comando, app e problemas comuns) está em [docs/como-rodar.md](docs/como-rodar.md). Resumo:

### Jeito mais fácil: `scripts/orbita.sh`

Requisitos: Docker Desktop, Java 21, Maven 3.9 e Node.js 20. No Windows, rode pelo Git Bash.

```bash
scripts/orbita.sh subir            # sobe tudo e abre o app em http://localhost:19006
scripts/orbita.sh subir --limpar   # o mesmo, apagando os dados (volta aos dados de demonstração)
scripts/orbita.sh status           # o que está no ar
scripts/orbita.sh abas             # reabre as abas de log no Windows Terminal
scripts/orbita.sh logs clientes    # log de um serviço no terminal atual (sem nome: todos)
scripts/orbita.sh parar            # desliga serviços, app e containers (os dados ficam)
```

O `subir` faz 5 passos, sempre na mesma ordem:

1. confere se Docker, Java, Maven e Node estão instalados;
2. sobe no Docker **só a infraestrutura**: RabbitMQ (5672, painel em 15672) e um MongoDB replica set (27018);
3. compila os 9 serviços com `mvn install` (se algum estiver rodando de antes, por exemplo pelo VS Code, ele é parado);
4. roda cada serviço com `mvn spring-boot:run`, em três ondas: primeiro quem só consome eventos (`auth`, `contas`, `negociacao`, `carteira`, `relatorios`), depois `pagamentos` e `ativos`, e por fim `clientes` e o `gateway`. Assim as filas já existem quando os dados de demonstração são publicados;
5. sobe o app com `npm run web` e abre http://localhost:19006 no navegador.

No fim, abre uma janela **OrbitaPay** no Windows Terminal com **uma aba por aplicação** (app, gateway e os 8 serviços), cada uma mostrando o log ao vivo. A aba fecha sozinha quando o serviço para. Sem o Windows Terminal, os logs de todos aparecem no próprio terminal. Se algo falhar, a janela espera um Enter antes de fechar, para dar tempo de ler o erro. Os serviços ficam em segundo plano e os logs também ficam em `.orbita-local/logs/`.

### Um comando para subir e testar tudo

Requisito: Docker Desktop (e Maven, opcional, para os testes unitários). No Windows, rode pelo Git Bash.

```bash
scripts/rodar-tudo.sh                 # testes unitários + sobe o ecossistema + teste k6
scripts/rodar-tudo.sh --limpar        # apaga os dados antes (volta às 10 ações de ORBT3)
scripts/rodar-tudo.sh --sem-unitarios # pula o Maven
scripts/rodar-tudo.sh --derrubar      # derruba os containers no final
```

O script roda os testes unitários dos 9 serviços, sobe tudo com `docker compose up --build --wait`, executa o teste de concorrência do k6 e termina com um resumo. Ele sai com código diferente de zero se alguma etapa falhar.

### Tudo em containers (recomendado)

Requisito: Docker Desktop.

```bash
docker compose up --build -d
```

O compose sobe o RabbitMQ, **um MongoDB por serviço**, os 8 microserviços e o gateway, todos na rede `orbita` com IP fixo. Ficam expostos no seu computador: **8080** (gateway, usado pelo app), **15672** (painel do RabbitMQ, usuário e senha `orbita`) e **5672** (AMQP). A ordem de subida garante que os consumidores declarem suas filas antes que Clientes e Ativos publiquem os dados iniciais.

> **Atualizando de uma versão anterior:** o PIN saiu do contexto de Clientes e passou para o `auth`, os serviços do compose perderam o sufixo `-service` e os projetos Maven passaram de `<contexto>-service` para `<contexto>-micro`. Recrie os dados de demonstração com `docker compose down -v --remove-orphans && docker compose up --build -d`.

### Painel do RabbitMQ

Abra http://localhost:15672 (usuário e senha `orbita`):

- **Exchanges** → `orbita.eventos` → *Bindings*: cada tópico e a fila que o assina;
- **Queues**: mensagens esperando, consumidores e os argumentos de cada fila (como a DLQ em `x-dead-letter-exchange`).

### Cada serviço na sua máquina

Use `scripts/orbita.sh` (veja [Jeito mais fácil](#jeito-mais-fácil-scriptsorbitash)). Ele sobe o RabbitMQ e um MongoDB **replica set** (porta 27018, exigido pelas transações de Negociação e Relatórios) no Docker e roda cada serviço com `mvn spring-boot:run` na ordem certa. Os `application.yaml` já apontam para esse MongoDB, então rodar um serviço pelo VS Code ou com `mvn -f backend/<servico>/springframework spring-boot:run` também funciona, desde que a infraestrutura esteja no ar (`docker compose --profile local up -d rabbitmq mongo-local`).

As variáveis `MONGODB_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` e `ORBITA_TOKEN_SEGREDO` sobrescrevem os padrões.

### App mobile

O app é escrito em **TypeScript (`.ts`/`.tsx`)** e usa **expo-router**: cada arquivo em `src/app` é uma rota.

```
mobile/src/
├── @types/        contratos do gateway (api.ts) e modelos das telas (orbita.ts)
├── app/           rotas: _layout, login, cadastro e (tabs)/ index · mercado · globo · banco · perfil
├── components/    ui/ (primitivas visuais), folhas/ (bottom sheets), Globo, Splash, barra de abas…
├── constants/     tema, bolsas, ícones, abas, dados de demonstração e formatação
├── context/       Toast, Sessao, Mercado, Gerente e Operacoes (estado do app)
│   └── operacoes/ ações separadas por assunto: banco, negociacao, gerencia e perfil
└── integration/   cliente HTTP, API do gateway, mapeadores DTO → modelo e sessão salva
```

```bash
cd mobile
npm install
npm run web         # abre o app no navegador em http://localhost:19006
npm run typecheck   # tsc --noEmit
```

**No navegador** (`npm run web`), o app fala com o gateway em `http://localhost:8080` e funciona por completo: login, depósito, saque, compra de ações com PIN, carteira, globo 3D e, para gerente e admin, a aba Banco. O script copia o `canvaskit.wasm` do Skia para `public/` e sobe o servidor web na porta 19006 (a 8081 é do serviço Clientes).

No **Android**, o app usa módulos nativos (Skia, Reanimated, react-native-screens), então roda em um *development build* e não no Expo Go: `npx expo run:android`.

Por padrão o app aponta para `http://10.0.2.2:8080` no emulador Android e para `http://localhost:8080` nas demais plataformas. Em um celular físico, defina o IP da sua máquina:

```bash
EXPO_PUBLIC_API_URL=http://192.168.0.10:8080 npx expo start
```

Contas de demonstração:

| E-mail | PIN | Observação |
|---|---|---|
| ana@orbita.com | 1234 | saldo R$ 12.480,55 |
| bruno@orbita.com | 4321 | saldo R$ 3.150,00 |
| carla@orbita.com | 1111 | bloqueada por PIN |
| gerente@orbita.com | 0000 | gerente: aba Banco com a gestão de clientes |
| admin@orbita.com | 9999 | admin: aba Banco com clientes e ações da bolsa |

## Autenticação

O contexto `auth` é o dono da credencial: e-mail de login, hash PBKDF2 do PIN e tentativas. Os demais contextos nunca veem o PIN.

- **Cadastro:** o PIN nunca passa pelo RabbitMQ. O app cria a credencial em `POST /api/autenticacao/credenciais` (e-mail e PIN), e o `auth` devolve o novo `clienteId` e uma sessão de cliente. Com essa sessão o app chama `POST /api/clientes`: o gateway valida o token e injeta o `X-Cliente-Id`, e Clientes cria o cadastro com esse id e publica `cliente.cadastrado`. Quando quem cadastra é o gerente, a chamada usa a sessão do gerente e leva o `clienteId` no corpo. Se o cadastro for recusado (CPF inválido, e-mail repetido), o app desfaz a credencial com `DELETE /api/autenticacao/credenciais/me`, que só vale nos 10 minutos depois da criação.
- **Login e assinatura:** `POST /api/autenticacao/sessoes` (e-mail e PIN) é o login de todas as contas e devolve o token de sessão e o `perfil` (`CLIENTE`, `GERENTE` ou `ADMIN`). `POST /api/autenticacao/assinaturas` confere o PIN e devolve a assinatura de 120 s exigida em saques, ordens e no desbloqueio pelo próprio cliente.
- **Tornar-se gerente:** na aba Perfil, o cliente escolhe *Tornar-me gerente* e confirma com o PIN. O app pede a assinatura e chama `POST /api/autenticacao/me/gerente` com `X-Assinatura`, e o gateway exige sessão de cliente **e** assinatura válidas. O `auth` troca a credencial do cliente por um funcionário `GERENTE` (mesmo e-mail e PIN) e devolve a sessão de gerente. Nada disso passa pelo RabbitMQ.
- **Bloqueio por PIN:** na 3ª tentativa errada o `auth` publica `credencial.bloqueada-por-pin`, Clientes bloqueia a conta com motivo `PIN` e publica `cliente.situacao-alterada` para Contas e Negociação. Quando a conta é desbloqueada, o `auth` zera as tentativas.
- O gateway continua validando os tokens localmente (HMAC com o segredo compartilhado), sem chamar o `auth` a cada requisição.

### Níveis de acesso

Cada conta tem um perfil. O perfil vai dentro do token, o gateway decide pela tabela de regras (`MapaDeAcesso`) e cada serviço confere de novo o cabeçalho `X-Perfil` (defesa em profundidade).

| Perfil | Quem é | O que pode | Abas no app |
|---|---|---|---|
| `CLIENTE` | quem abre conta no banco | a própria conta: saldo, depósito, saque, ordens, carteira, PIN | Início, Mercado, Globo, Perfil |
| `GERENTE` | funcionário do banco | listar, cadastrar, editar, bloquear e desbloquear clientes; ver contas, carteiras e o relatório gerencial | Banco, Mercado, Globo |
| `ADMIN` | administrador do banco | tudo do gerente + **excluir clientes** e **cadastrar, editar e remover ações** da bolsa | Banco, Mercado, Globo |

- As contas da equipe ficam na coleção `funcionarios` do `auth`, separadas das credenciais dos clientes, e são criadas na subida (`EquipeInicial`) ou quando um cliente se torna gerente. Um e-mail da equipe não pode ser usado para abrir conta de cliente.
- A aba **Banco não existe para o cliente**: o app monta as abas pelo perfil da sessão (`Tabs.Protected`), então ela nem aparece e o endereço `/banco` volta para o Início. Mesmo que alguém chame a API direto, o gateway responde `403`.
- A equipe acompanha o Mercado e o Globo, mas não compra nem vende ações (as rotas de cliente respondem `403` para gerente e admin).

## Depósitos (Pagamentos + camada anticorrupção)

O app não credita mais o saldo diretamente. O depósito é uma **cobrança** emitida no contexto de Pagamentos:

1. `POST /api/pagamentos {valor, metodo: PIX | BOLETO | TED}` aciona a **fachada** `ProvedorDePagamentos`, que escolhe o provedor pelo método e devolve as instruções (Pix copia e cola, linha digitável do boleto ou dados para a TED).
2. A conciliação (a cada 2 s) consulta os provedores pela mesma fachada. Quando a cobrança é paga, publica `pagamento.confirmado`. Quando expira, publica `pagamento.expirado`.
3. **Contas** trava a conta e credita o depósito uma única vez por `pagamentoId`.

Cada provedor tem um modelo próprio, e a camada anticorrupção (`pagamentos/.../acl`) traduz tudo para o modelo do OrbitaPay:

| Provedor (simulado) | Formato do provedor | Tradução |
|---|---|---|
| PSP Pix | API Pix do Banco Central: `txid`, valor `"10.00"`, status `ATIVA`/`CONCLUIDA`/`REMOVIDA_PELO_PSP`, horários ISO-8601 | `Dinheiro`, `AGUARDANDO`/`PAGA`/`EXPIRADA`, `Instant` |
| Banco emissor de boletos | valores em **centavos**, vencimento `yyyy-MM-dd`, liquidação em horário de Brasília **sem fuso**, situação `EM_ABERTO`/`LIQUIDADO`/`BAIXADO_POR_DECURSO_DE_PRAZO` | idem |
| SPB (TED) | valor em texto brasileiro `"1.234,56"`, datas `dd/MM/yyyy HH:mm:ss`, situação numérica `0`/`1`/`9` | idem |

Os provedores simulados pagam a cobrança depois de alguns segundos (Pix 3 s, TED 6 s, boleto 10 s). Valores terminados em **,99** nunca são pagos, para demonstrar a expiração.

## Relatórios

O contexto `relatorios` é um **modelo de leitura** (CQRS): assina `cliente.#`, `ordem.executada`, `ordem.rejeitada` e `pagamento.#` e grava cada mensagem como um fato, identificado pelo `eventoId` (a mesma mensagem entregue duas vezes não conta em dobro).

- `GET /api/relatorios/gerencial?de=2026-09-01&ate=2026-09-30` (gerente): base de clientes, volume comprado e vendido, taxa de rejeição das ordens, depósitos por método, ativos mais negociados, maiores investidores e movimento diário.
- `GET /api/relatorios/me?de=…&ate=…` (cliente): total depositado, comprado e vendido, resumo por ativo e últimos movimentos.

Sem datas, o período padrão são os últimos 30 dias.

## Compra de ações (saga + lock pessimista)

1. O app pede uma **assinatura** com o PIN (`POST /api/autenticacao/assinaturas`) e envia a ordem com `X-Assinatura` para `POST /api/ordens`.
2. A **Negociação** trava o ativo (lock pessimista), reserva as ações disponíveis e publica `ordem.compra-solicitada`. A resposta é `202 PENDENTE`.
3. **Contas** trava a conta, confere o saldo e o bloqueio, debita e publica `conta.debito-aprovado` (ou `recusado`).
4. A **Negociação** confirma a reserva (ou a devolve, como compensação) e publica `ordem.executada` ou `ordem.rejeitada`.
5. **Carteira** trava a carteira e registra a posição com preço médio.
6. O app consulta `GET /api/ordens/{id}` até a ordem sair de `PENDENTE`.

O ativo `ORBT3` (Órbita Holding) foi emitido com apenas **10 ações** justamente para demonstrar a concorrência.

## Testes com k6

O teste usa o gateway, como o app. Sem k6 instalado, rode pelo container do compose:

```bash
docker compose run --rm k6 run concorrencia.js
k6 run k6/concorrencia.js                                   # com o k6 instalado na máquina
k6 run -e QTD_REQUISICOES=30 -e TICKER=ORBT3 k6/concorrencia.js
```

O [`concorrencia.js`](k6/concorrencia.js) dispara **20 compras simultâneas** de 1 `ORBT3` (que tem só 10 ações) e acompanha cada ordem consultando `GET /api/ordens/{id}` até ela sair de `PENDENTE`:

1. `setup`: entra como a cliente Ana, gera a assinatura com o PIN e lê o estoque inicial em `GET /api/ofertas/ORBT3`;
2. cada usuário virtual envia `POST /api/ordens`. A resposta é `202` (ordem aceita) ou `422` (sem oferta disponível);
3. as ordens aceitas são consultadas até ficarem `EXECUTADA` ou `REJEITADA`;
4. `teardown`: lê o estoque final e confere se bate com o esperado.

| Métrica | Esperado num ambiente novo |
|---|---|
| `ordens_executadas` | 10 (o estoque inicial) |
| `ordens_rejeitadas` | 10 (requisições − estoque) |
| `ordens_timeout_polling` | 0 (se não for, aumente `POLL_TENTATIVAS`) |
| `estoque_inconsistente` | 0 (o estoque final nunca fica negativo nem diferente do esperado) |

As variáveis `BASE_URL`, `EMAIL`, `PIN`, `TICKER`, `QTD_REQUISICOES`, `POLL_TENTATIVAS` e `POLL_INTERVALO` mudam os padrões. As ações compradas ficam na carteira da Ana, então a oferta diminui a cada execução: rode `scripts/rodar-tudo.sh --limpar` (ou `docker compose down -v`) para voltar às 10 ações.

## Endpoints do gateway

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/api/autenticacao/sessoes` (login de cliente, gerente e admin) | público |
| POST | `/api/autenticacao/credenciais` (cria e-mail e PIN do novo cliente) | público |
| POST | `/api/autenticacao/assinaturas` · PUT `/api/autenticacao/pin` · GET `/api/autenticacao/me` · DELETE `/api/autenticacao/credenciais/me` | cliente |
| POST | `/api/autenticacao/me/gerente` (tornar-se gerente) | cliente + `X-Assinatura` |
| POST | `/api/clientes` | qualquer sessão: o cliente usa o `X-Cliente-Id` da sessão; gerente e admin informam `clienteId` e `depositoInicial` |
| GET/PUT/DELETE | `/api/clientes/me` · POST `/api/clientes/me/bloqueio` | cliente |
| POST | `/api/clientes/me/desbloqueio` | cliente + `X-Assinatura` |
| GET/PUT | `/api/clientes`, `/api/clientes/{id}` · POST `/bloqueio`, `/desbloqueio` | gerente ou admin |
| DELETE | `/api/clientes/{id}` | admin |
| GET | `/api/contas/me` | cliente |
| POST | `/api/contas/me/saques` | cliente + `X-Assinatura` |
| GET | `/api/contas` | gerente ou admin |
| POST/GET | `/api/pagamentos` · GET `/api/pagamentos/{id}` | cliente |
| GET | `/api/ativos`, `/api/ativos/{ticker}`, `/api/bolsas`, `/api/ofertas/{ticker}` | público |
| POST/PUT/DELETE | `/api/ativos[/{ticker}]` | admin |
| POST | `/api/ordens` | cliente + `X-Assinatura` |
| GET | `/api/ordens`, `/api/ordens/{id}` | cliente |
| GET | `/api/carteiras/me` · `/api/carteiras/{clienteId}` | cliente · gerente ou admin |
| GET | `/api/relatorios/me` · `/api/relatorios/gerencial` | cliente · gerente ou admin |
