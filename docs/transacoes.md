# Transações e consistência

Cada microserviço tem o seu próprio MongoDB, então **não existe uma transação que atravesse serviços**. A consistência do OrbitaPay é garantida em quatro níveis, do menor para o maior:

| Nível | Onde | Como |
|---|---|---|
| 1. Um documento | Contas, Carteira, Clientes, Auth, Ativos, Pagamentos | Toda gravação de um único documento no MongoDB é **atômica**. A conta guarda os lançamentos dentro dela; a carteira guarda as posições dentro dela. |
| 2. Transação no mesmo banco | Negociação e Relatórios | **Transação do MongoDB** (`@Transactional`) quando um caso de uso grava **dois documentos** no mesmo banco. |
| 3. Entre serviços | Compra e venda de ações | **Saga** orquestrada pela Negociação, com **compensação** (devolve a reserva se o débito for recusado). |
| 4. Mensagens repetidas | Todos os consumidores | **Idempotência** (a mesma mensagem processada duas vezes não altera nada) + 3 tentativas + **DLQ**. |

## Onde há transação e por quê

### Negociação: ativo + ordem

Quatro casos de uso alteram o estoque do ativo **e** o status da ordem: `EnviarOrdem` (compra), `ConfirmarCompra`, `CancelarCompra` e `ConfirmarVenda`. Sem transação, se a segunda gravação falhasse, o RabbitMQ entregaria a mensagem de novo e o estoque seria ajustado **duas vezes**.

Por isso o repositório grava os dois na mesma transação:

```java
@Override
@Transactional
public void salvarComOrdemELiberar(AtivoTravado travado, Ordem ordem) {
    trava.salvarELiberar(paraDocumento(travado.ativo()), travado.dono());
    ordens.salvar(ordem);
}
```

Ou grava **os dois**, ou **nenhum**. O caso de uso não sabe que existe transação: ele chama `trava.salvarComOrdemELiberar(travado, ordem)` na interface `TravaDeAtivo`, que fica no domínio (Java puro). O `@Transactional` fica só na implementação com MongoDB, no módulo `springframework`.

A **trava pessimista** é adquirida **antes** e **fora** da transação, de propósito: uma trava gravada dentro de uma transação só fica visível para as outras requisições depois do *commit*, e então não serviria para bloqueá-las ([lock-pessimista.md](lock-pessimista.md)).

### Relatórios: fato + perfil do cliente

Ao receber um evento, Relatórios grava o **fato** e atualiza o **perfil do cliente**. Os métodos do `EventosListener` são `@Transactional`: cada mensagem é processada inteira numa transação. A checagem de mensagem repetida é feita com `existsById` antes do insert, porque um erro de chave duplicada dentro de uma transação a aborta.

### Requisito do MongoDB

Transações no MongoDB só funcionam em **replica set**. Por isso, no `docker-compose.yml`, cada MongoDB sobe como um replica set de um nó só (`--replSet rs0`). O healthcheck inicia o replica set e só fica saudável quando o nó já aceita escrita, e os serviços se conectam com `?directConnection=true`. Cada serviço que usa transação declara um `MongoTransactionManager` em `infrastructure/config/TransacaoConfig.java`.

## Onde não precisa de transação

| Serviço | Por que não precisa |
|---|---|
| Contas | saldo e lançamentos ficam no **mesmo documento** da conta |
| Carteira | posições, reservas e ordens já liquidadas ficam no **mesmo documento** da carteira |
| Clientes, Auth, Ativos, Pagamentos | cada operação grava **um documento só** |

## Idempotência: a mesma mensagem duas vezes não estraga nada

| Consumidor | Como reconhece uma mensagem repetida |
|---|---|
| Contas | o lançamento guarda a referência (`ordemId` ou `pagamentoId`); se já existe, não debita nem credita de novo |
| Carteira | a carteira guarda as `ordensLiquidadas` e as reservas por `ordemId` |
| Negociação | só conclui ordens que ainda estão `PENDENTE` |
| Relatórios | o fato é identificado pelo `eventoId` |

## Limite conhecido

Gravar no banco e publicar no RabbitMQ são duas operações separadas. Se o RabbitMQ cair **exatamente** entre as duas, o dado fica gravado e o evento não sai. A solução clássica para isso é o padrão **Transactional Outbox**: gravar o evento numa coleção do próprio banco, na mesma transação, e publicar depois. O projeto não implementa o outbox, para manter o código simples. Nos testes, nenhuma mensagem se perdeu (todas as DLQs ficaram vazias).
