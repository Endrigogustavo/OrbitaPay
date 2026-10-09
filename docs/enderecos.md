# Endereços e IPs do ecossistema

Todos os containers ficam na rede Docker **`orbita`** (`172.30.0.0/24`), definida no [`docker-compose.yml`](../docker-compose.yml), e cada um tem um **IP fixo**.

Regra para decorar: **o final do IP de cada microserviço é a porta dele** (Clientes usa a porta `8081`, então o IP é `172.30.0.81`). O banco de cada serviço usa `1` na frente desse número (o MongoDB de Clientes é `172.30.0.181`).

## Acessos a partir do seu computador

Só estes três endereços são publicados para fora do Docker. O resto só é acessível de dentro da rede `orbita`.

| O que é | Endereço no seu computador | Usuário / senha |
|---|---|---|
| API Gateway (usado pelo app) | http://localhost:8080 | — |
| Painel do RabbitMQ (exchanges, filas e tópicos) | http://localhost:15672 | `orbita` / `orbita` |
| RabbitMQ (protocolo AMQP) | `localhost:5672` | `orbita` / `orbita` |

No celular físico, troque `localhost` pelo IP da sua máquina na rede Wi-Fi (veja [Como rodar](../README.md#app-mobile)).

## Infraestrutura

| Container | IP | Porta interna | Porta no seu computador |
|---|---|---|---|
| `rabbitmq` | `172.30.0.10` | `5672` (AMQP) · `15672` (painel) | `5672` · `15672` |
| `gateway` | `172.30.0.80` | `8080` | `8080` |

## Microserviços

| Serviço | Container | IP | Porta | Rotas do gateway que chegam nele | Banco (container · IP) |
|---|---|---|---|---|---|
| Clientes | `clientes` | `172.30.0.81` | `8081` | `/api/clientes/**` | `mongo-clientes` · `172.30.0.181` |
| Contas | `contas` | `172.30.0.82` | `8082` | `/api/contas/**` | `mongo-contas` · `172.30.0.182` |
| Ativos | `ativos` | `172.30.0.83` | `8083` | `/api/ativos/**`, `/api/bolsas` | `mongo-ativos` · `172.30.0.183` |
| Negociação | `negociacao` | `172.30.0.84` | `8084` | `/api/ordens/**`, `/api/ofertas/**` | `mongo-negociacao` · `172.30.0.184` |
| Carteira | `carteira` | `172.30.0.85` | `8085` | `/api/carteiras/**` | `mongo-carteira` · `172.30.0.185` |
| Auth | `auth` | `172.30.0.86` | `8086` | `/api/autenticacao/**` | `mongo-auth` · `172.30.0.186` |
| Pagamentos | `pagamentos` | `172.30.0.87` | `8087` | `/api/pagamentos/**` | `mongo-pagamentos` · `172.30.0.187` |
| Relatórios | `relatorios` | `172.30.0.88` | `8088` | `/api/relatorios/**` | `mongo-relatorios` · `172.30.0.188` |

Todos os MongoDB escutam na porta `27017` e cada serviço tem o seu banco: `orbita_clientes`, `orbita_contas`, `orbita_ativos`, `orbita_negociacao`, `orbita_carteira`, `orbita_auth`, `orbita_pagamentos` e `orbita_relatorios`.

## Quem fala com quem

| De | Para | Endereço usado | Protocolo |
|---|---|---|---|
| App mobile | Gateway | `http://localhost:8080` (emulador Android: `http://10.0.2.2:8080`) | HTTP |
| Gateway | cada microserviço | `http://<container>:<porta>` (ex.: `http://clientes:8081`) | HTTP |
| Cada microserviço | RabbitMQ | `rabbitmq:5672` | AMQP |
| Cada microserviço | o próprio MongoDB | `mongodb://mongo-<serviço>:27017/orbita_<serviço>` | MongoDB |
| k6 (testes) | Gateway | `http://gateway:8080` | HTTP |

Dentro da rede, os serviços se chamam pelo **nome do container** (o Docker resolve o nome para o IP da tabela). Microserviços **nunca** se chamam por HTTP entre si: toda conversa entre eles passa pelo RabbitMQ (veja o [diagrama de comunicação](comunicacao.md)).

## Comandos úteis

```bash
docker network inspect orbitapay_orbita                     # todos os containers e IPs da rede
docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' orbitapay-contas-1
docker compose ps                                           # situação de cada container
```

> Como cada container tem um IP fixo, não dá para usar `docker compose up --scale` (duas cópias não podem ter o mesmo IP). Para testar o lock pessimista com várias instâncias, remova o `ipv4_address` do serviço no `docker-compose.yml` antes de escalar.
