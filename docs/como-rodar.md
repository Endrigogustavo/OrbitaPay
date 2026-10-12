# Como rodar o OrbitaPay

Guia passo a passo para subir e testar o ecossistema inteiro numa máquina nova. Os comandos são para **bash**: no Windows, use o **Git Bash**, que vem junto com o Git.

## 1. O que você precisa instalar

| Ferramenta | Para quê | Obrigatória? |
|---|---|---|
| [Docker Desktop](https://www.docker.com/products/docker-desktop/) | Roda os 18 containers (8 microserviços, gateway, RabbitMQ e 8 MongoDB) | **Sim** |
| [Git](https://git-scm.com/) (no Windows, com o Git Bash) | Baixar o projeto e rodar o script | **Sim** |
| Java 21 + Maven 3.9 | Rodar os testes unitários antes de subir | Não (o Docker compila tudo sozinho) |
| Node.js 20 ou mais novo | Rodar o app mobile | Só para o app |
| Android Studio (com um emulador) | Abrir o app mobile | Só para o app |

**Memória:** o ecossistema usa cerca de **4,5 GB de RAM**. No Docker Desktop, em *Settings → Resources*, deixe **pelo menos 6 GB** de memória.

**Portas que precisam estar livres:** `8080` (gateway), `5672` e `15672` (RabbitMQ).

## 2. Baixar o projeto

```bash
git clone https://github.com/Endrigogustavo/OrbitaPay.git
cd OrbitaPay
```

## 3. Subir tudo com um comando

Com o Docker Desktop aberto:

```bash
scripts/rodar-tudo.sh --limpar
```

O script faz, nesta ordem:

1. roda os testes unitários dos 9 serviços com Maven (pulado se o Maven não estiver instalado);
2. apaga os dados antigos (`--limpar`) e sobe os 18 containers com `docker compose up --build --wait`, esperando todos ficarem saudáveis;
3. roda o teste de concorrência do k6 (20 compras simultâneas de uma ação que só tem 10 unidades);
4. mostra um resumo.

A **primeira vez demora de 5 a 10 minutos**, porque o Docker baixa as imagens e as dependências Maven. Nas próximas, leva cerca de 2 minutos. O resultado esperado no final é:

```
[OK]    Maven: auth
...
[OK]    Ecossistema no ar (todos os containers saudaveis)
[OK]    k6: compra simultanea (lock pessimista)

Tudo certo. Gateway em http://localhost:8080 e painel do RabbitMQ em http://localhost:15672 (orbita/orbita).
```

Outras opções do script:

| Comando | O que faz |
|---|---|
| `scripts/rodar-tudo.sh` | igual, mas **mantém os dados** de execuções anteriores |
| `scripts/rodar-tudo.sh --sem-unitarios` | pula o Maven (mais rápido) |
| `scripts/rodar-tudo.sh --derrubar` | desliga os containers no final |
| `scripts/rodar-tudo.sh --ajuda` | mostra as opções |

Sem o script, o mesmo resultado (sem os testes) sai com:

```bash
docker compose up --build -d --wait
```

## 4. Conferir se está tudo no ar

```bash
docker compose ps
```

Os 18 containers devem aparecer como `healthy`. Depois, abra no navegador:

| O que é | Endereço | Usuário / senha |
|---|---|---|
| API Gateway (lista os ativos, sem login) | http://localhost:8080/api/ativos | — |
| Painel do RabbitMQ | http://localhost:15672 | `orbita` / `orbita` |

Os IPs fixos de cada container estão em [enderecos.md](enderecos.md).

## 5. Testar pela linha de comando

Uma compra de ação completa, do login até a carteira, só com `curl`. Copie e cole os blocos em ordem, no mesmo terminal.

**Entrar como a cliente de demonstração Ana** (e-mail `ana@orbita.com`, PIN `1234`) e guardar o token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/autenticacao/clientes \
  -H "Content-Type: application/json" -d '{"email":"ana@orbita.com","pin":"1234"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "$TOKEN"
```

**Ver o saldo:**

```bash
curl -s http://localhost:8080/api/contas/me -H "Authorization: Bearer $TOKEN"
```

**Gerar a assinatura com o PIN** (vale 120 segundos e é exigida em saques e ordens):

```bash
ASSINATURA=$(curl -s -X POST http://localhost:8080/api/autenticacao/assinaturas \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"pin":"1234"}' \
  | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

**Comprar 1 ação da Petrobras (PETR4).** A resposta é `202` com a ordem `PENDENTE`, porque a compra continua pela saga no RabbitMQ:

```bash
ORDEM=$(curl -s -X POST http://localhost:8080/api/ordens \
  -H "Authorization: Bearer $TOKEN" -H "X-Assinatura: $ASSINATURA" -H "Content-Type: application/json" \
  -d '{"ticker":"PETR4","tipo":"COMPRA","quantidade":1}' \
  | sed -E 's/.*"id":"([^"]+)".*/\1/')
echo "$ORDEM"
```

**Acompanhar a ordem** (em menos de um segundo ela fica `EXECUTADA`):

```bash
curl -s http://localhost:8080/api/ordens/$ORDEM -H "Authorization: Bearer $TOKEN"
```

**Ver a ação na carteira:**

```bash
curl -s http://localhost:8080/api/carteiras/me -H "Authorization: Bearer $TOKEN"
```

**Ver o gateway barrando acessos:**

```bash
curl -s -o /dev/null -w "sem login: %{http_code}\n" http://localhost:8080/api/contas/me
curl -s -o /dev/null -w "cliente em rota de gerente: %{http_code}\n" http://localhost:8080/api/clientes -H "Authorization: Bearer $TOKEN"
```

O primeiro responde `401` (precisa entrar) e o segundo `403` (o perfil não tem permissão). A lista completa de rotas está no [README](../README.md#endpoints-do-gateway).

## 6. Ver as mensagens no RabbitMQ

Abra http://localhost:15672 (usuário e senha `orbita`):

- **Exchanges → `orbita.eventos` → Bindings:** cada tópico (como `ordem.executada`) e a fila de cada serviço que o recebe;
- **Queues:** as 28 filas, com mensagens e consumidores. As filas terminadas em `.dlq` guardam mensagens que falharam 3 vezes e devem ficar **vazias**.

Para entender quem fala com quem, veja o [diagrama de comunicação](comunicacao.md).

## 7. Rodar o teste de concorrência (k6)

```bash
docker compose run --rm k6 run concorrencia.js
```

Ele dispara 20 compras simultâneas de `ORBT3`, que tem só **10 ações**, e confere o estoque no final. Num ambiente recém-criado, o resultado esperado é:

```
Estoque inicial:          10
Estoque final no banco:   0
Estoque final bateu com o esperado.
ordens_executadas..............: 10
ordens_rejeitadas..............: 10
```

As ações compradas ficam com a Ana, então a cada execução o estoque de `ORBT3` diminui. Para voltar às 10 ações, recrie os dados com `scripts/rodar-tudo.sh --limpar` (ou `docker compose down -v` e `docker compose up --build -d --wait`).

Para mudar a quantidade de compras ou o ativo:

```bash
docker compose run --rm k6 run -e QTD_REQUISICOES=30 -e TICKER=VALE3 concorrencia.js
```

Com o k6 instalado na própria máquina, o comando é `k6 run k6/concorrencia.js`.

## 8. Rodar o app mobile

Com o ecossistema no ar:

```bash
cd mobile
npm install
npx expo run:android
```

O app usa módulos nativos (Skia, Reanimated), então roda num **development build** e não no Expo Go. O `expo run:android` compila e instala o app no emulador aberto no Android Studio. No emulador, o app encontra o gateway sozinho em `http://10.0.2.2:8080`.

Num **celular físico** na mesma rede Wi-Fi, informe o IP do seu computador:

```bash
EXPO_PUBLIC_API_URL=http://192.168.0.10:8080 npx expo run:android
```

Contas de demonstração:

| E-mail | PIN | Observação |
|---|---|---|
| ana@orbita.com | 1234 | saldo inicial R$ 12.480,55 |
| bruno@orbita.com | 4321 | saldo inicial R$ 3.150,00 |
| carla@orbita.com | 1111 | bloqueada por PIN (para mostrar o desbloqueio) |
| código do gerente | 0000 | aba Banco |

## 9. Parar

```bash
docker compose down        # desliga e mantém os dados
docker compose down -v     # desliga e apaga os dados (volta aos dados de demonstração)
```

## 10. Problemas comuns

| Sintoma | Causa | O que fazer |
|---|---|---|
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | outro programa usa a porta 8080, 5672 ou 15672 | feche o programa ou pare o outro container |
| `Pool overlaps with other one on this address space` | outra rede Docker já usa a faixa `172.30.0.0/24` | `docker network ls` e remova a rede que conflita, ou troque a faixa no `docker-compose.yml` |
| O build falha com `Could not transfer artifact ... from/to central` | instabilidade de rede ao baixar dependências Maven | rode o mesmo comando de novo (o que já foi baixado fica em cache) |
| Containers ficam `unhealthy` ou caem | pouca memória no Docker | aumente para 6 GB ou mais em *Settings → Resources* |
| Um container específico não sobe | erro no serviço | `docker compose logs <serviço>` (ex.: `docker compose logs negociacao`) |
| `$'\r': command not found` ao rodar o script | o arquivo foi salvo com fim de linha do Windows | `git checkout -- scripts/rodar-tudo.sh` (o `.gitattributes` já força o formato certo) |
| k6 mostra `Estoque inicial: 0` | as 10 ações de `ORBT3` já foram compradas antes | `scripts/rodar-tudo.sh --limpar` |
| O app no celular não conecta | o celular não alcança o `localhost` do computador | use `EXPO_PUBLIC_API_URL` com o IP do computador e libere a porta 8080 no firewall |
