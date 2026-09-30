# OrbitaPay

Ecossistema de **microserviços** para o banco e a corretora Órbita. Cada serviço corresponde a um **bounded context (DDD)** e segue **Clean Architecture**. A comunicação entre serviços é feita exclusivamente por **mensageria (RabbitMQ)**. O app **React Native (Expo)** fala somente com o **API Gateway**.

- Documentação do Context Map, da saga e do lock pessimista: [docs/context-map.md](docs/context-map.md)
- Diagramas UML em PlantUML (componentes, classes, sequências, estados): [docs/plantuml/](docs/plantuml/README.md)

## Serviços

| Serviço | Porta | Banco MongoDB | Responsabilidade |
|---|---|---|---|
| `gateway` | 8080 | — | Entrada única do front. Roteia, valida sessão (HMAC), exige assinatura por PIN e injeta a identidade |
| `clientes-service` | 8081 | `orbita_clientes` | Cadastro, login por PIN, bloqueio, sessão do gerente |
| `contas-service` | 8082 | `orbita_contas` | Saldo, extrato, depósito, saque, débito e crédito das ordens |
| `ativos-service` | 8083 | `orbita_ativos` | Catálogo de ações, bolsas, cotações simuladas ao vivo |
| `negociacao-service` | 8084 | `orbita_negociacao` | Ordens de compra e venda (saga), oferta disponível por ativo |
| `carteira-service` | 8085 | `orbita_carteira` | Custódia: posições, preço médio, reservas de venda |

Cada pasta em `backend/` é um projeto Maven **independente** (Spring Boot 4.0 / Java 21): `pom.xml`, `Dockerfile`, `application.yaml` e banco próprios. Não existe código nem banco compartilhado.

Por dentro, cada serviço segue a separação de um **monolito modular**, com dois módulos Maven:

| Módulo | Conteúdo | Dependências |
|---|---|---|
| `domain` | núcleo: `domain` (entidades, value objects, eventos, exceções) e `application` (casos de uso e portas) | **nenhuma**: Java puro, sem Spring, MongoDB, RabbitMQ ou Jackson |
| `springframework` | aplicação Spring Boot: `adapter` (web, mensageria, persistência), `infrastructure` (configuração, seed) e `application.yaml` | `domain` + starters do Spring |

O `pom.xml` na raiz de cada serviço só agrega os dois módulos e não herda do Spring, então nada do framework chega ao `domain`. Como o `domain` não declara dependências, qualquer `import org.springframework...` nele quebra a compilação.

```
OrbitaPay/
├── backend/
│   ├── gateway/
│   ├── clientes-service/
│   │   ├── domain/              núcleo em Java puro (pom sem dependências)
│   │   ├── springframework/     Spring Boot, adapters e configuração
│   │   ├── pom.xml              agregador dos módulos
│   │   └── Dockerfile
│   ├── contas-service/
│   ├── ativos-service/
│   ├── negociacao-service/
│   └── carteira-service/
├── mobile/                  app Expo / React Native em TypeScript (expo-router)
├── docs/context-map.md
├── scripts/teste-concorrencia.sh
└── docker-compose.yml
```

## Como rodar

### Tudo em containers (recomendado)

Requisito: Docker Desktop.

```bash
docker compose up --build -d
```

O compose sobe o RabbitMQ (painel em http://localhost:15672, usuário e senha `orbita`), **um MongoDB por serviço**, os 5 microserviços e o gateway. Só a porta **8080** (gateway) fica exposta para o front. A ordem de subida garante que os consumidores declarem suas filas antes que Clientes e Ativos publiquem os dados iniciais.

Para escalar um serviço e ver o lock funcionando entre instâncias:

```bash
docker compose up -d --scale negociacao-service=2 --scale contas-service=2
```

### Cada serviço na sua máquina

Suba só a infraestrutura com `docker compose up -d rabbitmq`, mais um MongoDB em `localhost:27017`. Depois, em cada pasta, rode `mvn install -DskipTests` e em seguida `mvn -f springframework spring-boot:run`, nesta ordem:

1. `contas-service`, `negociacao-service`, `carteira-service` (consumidores; declaram as filas)
2. `ativos-service`, `clientes-service` (publicam os dados iniciais)
3. `gateway`

As variáveis `MONGODB_URI`, `RABBITMQ_HOST`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` e `ORBITA_TOKEN_SEGREDO` sobrescrevem os padrões.

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

## Compra de ações (saga + lock pessimista)

1. O app pede uma **assinatura** com o PIN (`POST /api/clientes/me/assinaturas`) e envia a ordem com `X-Assinatura` para `POST /api/ordens`.
2. A **Negociação** trava o ativo (lock pessimista), reserva as ações disponíveis e publica `ordem.compra-solicitada`. A resposta é `202 PENDENTE`.
3. **Contas** trava a conta, confere o saldo e o bloqueio, debita e publica `conta.debito-aprovado` (ou `recusado`).
4. A **Negociação** confirma a reserva (ou a devolve, como compensação) e publica `ordem.executada` ou `ordem.rejeitada`.
5. **Carteira** trava a carteira e registra a posição com preço médio.
6. O app consulta `GET /api/ordens/{id}` até a ordem sair de `PENDENTE`.

O ativo `ORBT3` (Órbita Holding) foi emitido com apenas **10 ações** justamente para demonstrar a concorrência.

### Teste de concorrência

Com o ecossistema no ar:

```bash
bash scripts/teste-concorrencia.sh
```

O script dispara, ao mesmo tempo:
- 20 ordens de compra de 1 `ORBT3` para 10 ações disponíveis. Esperado: 10 aceitas, 10 recusadas, oferta final 0;
- 10 saques de R$ 500 numa conta com R$ 3.150. Esperado: 6 aprovados e saldo final R$ 150, nunca negativo.

Ele grava um relatório `.txt`, no mesmo formato do teste de concorrência do projeto de estoque.

## Endpoints do gateway

| Método | Caminho | Acesso |
|---|---|---|
| POST | `/api/autenticacao/clientes` · `/api/autenticacao/gerente` | público |
| POST | `/api/clientes` | público (gerente pode informar `depositoInicial`) |
| GET/PUT/DELETE | `/api/clientes/me` · PUT `/api/clientes/me/pin` | cliente |
| POST | `/api/clientes/me/assinaturas` · `/bloqueio` · `/desbloqueio` | cliente |
| GET/PUT/DELETE | `/api/clientes`, `/api/clientes/{id}`, `/bloqueio`, `/desbloqueio` | gerente |
| GET | `/api/contas/me` · POST `/api/contas/me/depositos` | cliente |
| POST | `/api/contas/me/saques` | cliente + `X-Assinatura` |
| GET | `/api/contas` | gerente |
| GET | `/api/ativos`, `/api/ativos/{ticker}`, `/api/bolsas`, `/api/ofertas/{ticker}` | público |
| POST/PUT/DELETE | `/api/ativos[/{ticker}]` | gerente |
| POST | `/api/ordens` | cliente + `X-Assinatura` |
| GET | `/api/ordens`, `/api/ordens/{id}` | cliente |
| GET | `/api/carteiras/me` · `/api/carteiras/{clienteId}` | cliente · gerente |
