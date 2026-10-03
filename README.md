# OrbitaPay

Ecossistema de **microserviços** para o banco e a corretora Órbita. Cada serviço corresponde a um **bounded context (DDD)** e é organizado em **camadas** (domínio, aplicação e infraestrutura). A comunicação entre serviços é feita exclusivamente por **mensageria (RabbitMQ)**. O app **React Native (Expo)** fala somente com o **API Gateway**.

- Documentação do Context Map, da saga, das camadas anticorrupção e do lock pessimista: [docs/context-map.md](docs/context-map.md)
- Por que o MongoDB não tem lock pessimista e como ele foi implementado: [docs/lock-pessimista.md](docs/lock-pessimista.md)
- Diagramas UML em PlantUML: [docs/plantuml/](docs/plantuml/README.md)
- Testes de carga e concorrência com k6: [k6/](k6/README.md)

## Serviços

| Serviço | Porta | Banco MongoDB | Responsabilidade |
|---|---|---|---|
| `gateway` | 8080 | — | Entrada única do front. Roteia, valida sessão (HMAC), exige assinatura por PIN e injeta a identidade |
| `auth` | 8086 | `orbita_auth` | Credenciais (PIN), login do cliente e do gerente, tokens de sessão e de assinatura, bloqueio por tentativas |
| `clientes` | 8081 | `orbita_clientes` | Cadastro, dados pessoais e situação da conta (bloqueio e desbloqueio) |
| `contas` | 8082 | `orbita_contas` | Saldo, extrato, saque, crédito dos depósitos confirmados, débito e crédito das ordens |
| `ativos` | 8083 | `orbita_ativos` | Catálogo de ações, bolsas, cotações simuladas ao vivo |
| `negociacao` | 8084 | `orbita_negociacao` | Ordens de compra e venda (saga), oferta disponível por ativo |
| `carteira` | 8085 | `orbita_carteira` | Custódia: posições, preço médio, reservas de venda |
| `pagamentos` | 8087 | `orbita_pagamentos` | Depósitos por Pix, boleto e TED: fachada com camada anticorrupção sobre os provedores |
| `relatorios` | 8088 | `orbita_relatorios` | Relatório gerencial e extrato de investimentos, montados a partir dos eventos |

Cada pasta em `backend/` é um projeto Maven **independente** (Spring Boot 4.0 / Java 21): `pom.xml`, `Dockerfile`, `application.yaml` e banco próprios. Não existe código nem banco compartilhado. Os microserviços usam o sufixo `-micro` no `artifactId` e no `spring.application.name` (`clientes-micro`, `contas-micro`…); a pasta e o serviço do compose levam só o nome do contexto (`clientes`, `contas`…).

### Camadas de cada serviço

Por dentro, cada serviço tem dois módulos Maven. Os casos de uso são **classes concretas**, chamadas diretamente pelos controllers e listeners, sem interfaces de "porta de entrada". Só continua sendo interface o que o núcleo em Java puro precisa que a infraestrutura implemente: os repositórios e alguns serviços técnicos, como publicar eventos ou codificar o PIN.

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
├── k6/                      testes de fumaça, carga, jornada e concorrência
├── scripts/rodar-tudo.sh    sobe o ecossistema e roda todos os testes
├── docs/                    context-map.md e diagramas PlantUML
└── docker-compose.yml
```

## Como rodar

### Um comando para subir e testar tudo

Requisito: Docker Desktop (e Maven, opcional, para os testes unitários). No Windows, rode pelo Git Bash.

```bash
scripts/rodar-tudo.sh                 # testes unitários + sobe o ecossistema + testes k6
scripts/rodar-tudo.sh --limpar        # apaga os dados antes (resultados exatos do teste de concorrência)
scripts/rodar-tudo.sh --sem-unitarios # pula o Maven
scripts/rodar-tudo.sh --derrubar      # derruba os containers no final
```

O script roda os testes unitários dos 9 serviços, sobe tudo com `docker compose up --build --wait`, executa os testes k6 de fumaça, concorrência, jornada do investidor e carga, e termina com um resumo. Ele sai com código diferente de zero se alguma etapa falhar.

### Tudo em containers (recomendado)

Requisito: Docker Desktop.

```bash
docker compose up --build -d
```

O compose sobe o RabbitMQ (painel em http://localhost:15672, usuário e senha `orbita`), **um MongoDB por serviço**, os 8 microserviços e o gateway. Só a porta **8080** (gateway) fica exposta para o front. A ordem de subida garante que os consumidores declarem suas filas antes que Clientes e Ativos publiquem os dados iniciais.

> **Atualizando de uma versão anterior:** o PIN saiu do contexto de Clientes e passou para o `auth`, os serviços do compose perderam o sufixo `-service` e os projetos Maven passaram de `<contexto>-service` para `<contexto>-micro`. Recrie os dados de demonstração com `docker compose down -v --remove-orphans && docker compose up --build -d`.

Para escalar um serviço e ver o lock funcionando entre instâncias:

```bash
docker compose up -d --scale negociacao=2 --scale contas=2
```

### Cada serviço na sua máquina

Suba só a infraestrutura com `docker compose up -d rabbitmq`, mais um MongoDB em `localhost:27017`. Depois, em cada pasta, rode `mvn install -DskipTests` e em seguida `mvn -f springframework spring-boot:run`, nesta ordem:

1. `auth`, `contas`, `negociacao`, `carteira`, `relatorios` (consumidores; declaram as filas)
2. `pagamentos`, `ativos`, `clientes` (publicam; o cadastro dos clientes de demonstração precisa do `auth` no ar)
3. `gateway`

As variáveis `MONGODB_URI`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` e `ORBITA_TOKEN_SEGREDO` sobrescrevem os padrões.

### App mobile

O app é escrito em **TypeScript (`.ts`/`.tsx`)** e usa **expo-router**: cada arquivo em `src/app` é uma rota.

```
mobile/src/
├── @types/        contratos do gateway (api.ts) e modelos das telas (orbita.ts)
├── app/           rotas: _layout, login, cadastro e (tabs)/ index · mercado · globo · banco · perfil
├── components/    ui/ (primitivas visuais), folhas/ (bottom sheets), Globo, Splash, barra de abas…
├── constants/     tema, bolsas, ícones, abas, dados de demonstração e formatação
├── context/       Toast, Sessao, Mercado, Gerente e Operacoes (estado e regras do app)
└── integration/   cliente HTTP, API do gateway, mapeadores DTO → modelo e sessão salva
```

```bash
cd mobile
npm install
npx expo start
npm run typecheck   # tsc --noEmit
```

O app usa módulos nativos (Skia, Reanimated, react-native-screens), então roda em um *development build* e não no Expo Go: `npx expo run:android`.

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
| código do gerente | 0000 | aba Banco |

## Autenticação

O contexto `auth` é o dono da credencial: e-mail de login, hash PBKDF2 do PIN e tentativas. Os demais contextos nunca veem o PIN.

- **Cadastro:** `POST /api/clientes` chega em Clientes, que pede a criação da credencial ao `auth` por **request/reply** na fila ponto a ponto `auth.registros-de-credencial`. O cliente só é anunciado (`cliente.cadastrado`) depois que a credencial existe. Se o `auth` recusar o PIN, o cadastro é desfeito.
- **Login e assinatura:** `POST /api/autenticacao/clientes` devolve o token de sessão. `POST /api/autenticacao/assinaturas` confere o PIN e devolve a assinatura de 120 s exigida em saques, ordens e no desbloqueio pelo próprio cliente.
- **Bloqueio por PIN:** na 3ª tentativa errada o `auth` publica `credencial.bloqueada-por-pin`, Clientes bloqueia a conta com motivo `PIN` e publica `cliente.situacao-alterada` para Contas e Negociação. Quando a conta é desbloqueada, o `auth` zera as tentativas.
- O gateway continua validando os tokens localmente (HMAC com o segredo compartilhado), sem chamar o `auth` a cada requisição.

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

Os scripts em [`k6/`](k6/README.md) usam o gateway, como o app. Sem k6 instalado, rode pelo container do compose:

```bash
docker compose run --rm k6 run smoke.js          # percorre todos os contextos uma vez
docker compose run --rm k6 run concorrencia.js   # 20 compras de ORBT3 + 10 saques simultâneos
docker compose run --rm k6 run carga-mercado.js  # leituras públicas em taxa constante
docker compose run --rm k6 run jornada-investidor.js
```

O teste de concorrência dispara, ao mesmo tempo:
- 20 ordens de compra de 1 `ORBT3` para 10 ações disponíveis. Esperado: 10 executadas, oferta final 0;
- 10 saques de R$ 500 numa conta com R$ 3.150. Esperado: 6 aprovados e saldo final R$ 150, nunca negativo.

No fim, ele confere o estado real dos serviços (oferta, ordens e extrato) e grava um relatório `relatorio-concorrencia-<data>.txt`.

## Endpoints do gateway

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/api/autenticacao/clientes` · `/api/autenticacao/gerente` | público |
| POST | `/api/autenticacao/assinaturas` · PUT `/api/autenticacao/pin` · GET `/api/autenticacao/me` | cliente |
| POST | `/api/clientes` | público (gerente pode informar `depositoInicial`) |
| GET/PUT/DELETE | `/api/clientes/me` · POST `/api/clientes/me/bloqueio` | cliente |
| POST | `/api/clientes/me/desbloqueio` | cliente + `X-Assinatura` |
| GET/PUT/DELETE | `/api/clientes`, `/api/clientes/{id}`, `/bloqueio`, `/desbloqueio` | gerente |
| GET | `/api/contas/me` | cliente |
| POST | `/api/contas/me/saques` | cliente + `X-Assinatura` |
| GET | `/api/contas` | gerente |
| POST/GET | `/api/pagamentos` · GET `/api/pagamentos/{id}` | cliente |
| GET | `/api/ativos`, `/api/ativos/{ticker}`, `/api/bolsas`, `/api/ofertas/{ticker}` | público |
| POST/PUT/DELETE | `/api/ativos[/{ticker}]` | gerente |
| POST | `/api/ordens` | cliente + `X-Assinatura` |
| GET | `/api/ordens`, `/api/ordens/{id}` | cliente |
| GET | `/api/carteiras/me` · `/api/carteiras/{clienteId}` | cliente · gerente |
| GET | `/api/relatorios/me` · `/api/relatorios/gerencial` | cliente · gerente |
