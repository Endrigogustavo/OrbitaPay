# Documentação em PlantUML — OrbitaPay

Diagramas UML do ecossistema OrbitaPay, derivados do código em `backend/`. Complementam o [Context Map](../context-map.md).

## Arquitetura

| Arquivo | Tipo | Conteúdo |
|---|---|---|
| [01-visao-geral.puml](01-visao-geral.puml) | Componentes | App, Gateway, 5 microserviços, bancos MongoDB e RabbitMQ |
| [02-context-map.puml](02-context-map.puml) | Componentes | Bounded contexts e padrões DDD (U/D, OHS/PL, ACL, Parceria) |
| [03-implantacao.puml](03-implantacao.puml) | Implantação | Containers do `docker-compose.yml`, portas e ordem de subida |
| [18-mensageria.puml](18-mensageria.puml) | Componentes | Exchange `orbita.eventos`, filas por serviço, `ativos.consultas` e DLQs |
| [19-clean-architecture.puml](19-clean-architecture.puml) | Pacotes/classes | Camadas domain → application → adapter → infrastructure (negociacao-service) |

## Requisitos

| Arquivo | Tipo | Conteúdo |
|---|---|---|
| [04-casos-de-uso.puml](04-casos-de-uso.puml) | Casos de uso | Visitante, Cliente, Gerente e Agendador |

## Modelo de domínio (classes)

| Arquivo | Contexto |
|---|---|
| [05-dominio-clientes.puml](05-dominio-clientes.puml) | Clientes: `Cliente`, VOs `NomeCompleto`, `Email`, `Cpf`, `Pin` |
| [06-dominio-contas.puml](06-dominio-contas.puml) | Contas: `Conta`, `Lancamento`, `Dinheiro` |
| [07-dominio-ativos.puml](07-dominio-ativos.puml) | Ativos: `Ativo`, `Ticker`, `Bolsa` |
| [08-dominio-negociacao.puml](08-dominio-negociacao.puml) | Negociação: `Ordem`, `AtivoNegociavel`, `Investidor` |
| [09-dominio-carteira.puml](09-dominio-carteira.puml) | Carteira: `Carteira`, `Posicao`, `AtivoCotado` |
| [10-gateway.puml](10-gateway.puml) | Gateway: `ControleDeAcesso`, `MapaDeAcesso`, `Credencial`, `Decisao` |

## Comportamento

| Arquivo | Tipo | Conteúdo |
|---|---|---|
| [11-seq-login-assinatura.puml](11-seq-login-assinatura.puml) | Sequência | Login por PIN, bloqueio após 3 erros, token de assinatura (120 s) |
| [12-seq-saga-compra.puml](12-seq-saga-compra.puml) | Sequência | Saga de compra com reserva, débito e compensação |
| [13-seq-saga-venda.puml](13-seq-saga-venda.puml) | Sequência | Saga de venda com reserva na carteira e crédito na conta |
| [14-seq-trava-pessimista.puml](14-seq-trava-pessimista.puml) | Sequência | `TravaPessimistaMongo`: adquirir, salvar+liberar, liberar |
| [15-seq-cotacoes.puml](15-seq-cotacoes.puml) | Sequência | Simulação de cotações a cada 3 s e propagação das projeções |
| [16-estados.puml](16-estados.puml) | Estados | Ciclo de vida da `Ordem` e do bloqueio do `Cliente` |
| [17-atividade-gateway.puml](17-atividade-gateway.puml) | Atividade | Decisão de acesso do gateway por requisição |

## Como gerar as imagens

**VS Code:** instale a extensão *PlantUML* (jebbs.plantuml), abra um `.puml` e use `Alt+D` para a pré-visualização.

**Linha de comando** (Java 21 já é requisito do backend):

```bash
mvn dependency:get -Dartifact=net.sourceforge.plantuml:plantuml:1.2024.8
java -jar ~/.m2/repository/net/sourceforge/plantuml/plantuml/1.2024.8/plantuml-1.2024.8.jar \
  -charset UTF-8 -Playout=smetana -tpng -o out docs/plantuml/*.puml
```

`-Playout=smetana` usa o motor de layout embutido e dispensa a instalação do Graphviz. Troque `-tpng` por `-tsvg` para gerar SVG.
