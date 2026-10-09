# Documentação em PlantUML — OrbitaPay

Diagramas UML do ecossistema OrbitaPay, derivados do código em `backend/`. Complementam o [diagrama de comunicação](../comunicacao.md) e o [Context Map](../context-map.md), que traz também a saga de compra, o modelo de cada contexto e a trava pessimista.

## Arquitetura

| Arquivo | Tipo | Conteúdo |
|---|---|---|
| [01-visao-geral.puml](01-visao-geral.puml) | Componentes | App, Gateway, 8 microserviços, bancos MongoDB e RabbitMQ |
| [02-context-map.puml](02-context-map.puml) | Componentes | Bounded contexts e padrões DDD (U/D, OHS/PL, ACL, Parceria) |
| [03-implantacao.puml](03-implantacao.puml) | Implantação | Containers do `docker-compose.yml`, IPs fixos, portas e ordem de subida |
| [06-mensageria.puml](06-mensageria.puml) | Componentes | Exchange `orbita.eventos`, filas por serviço, request/reply (`ativos.consultas`, `auth.registros-de-credencial`) e DLQs |

## Gateway

| Arquivo | Tipo | Conteúdo |
|---|---|---|
| [04-gateway.puml](04-gateway.puml) | Classes | `ControleDeAcesso`, `MapaDeAcesso` (tabela de `RegraDeAcesso`), `Credencial`, `Decisao` e o filtro HTTP |
| [05-atividade-gateway.puml](05-atividade-gateway.puml) | Atividade | Decisão de acesso do gateway por requisição |

## Como gerar as imagens

**VS Code:** instale a extensão *PlantUML* (jebbs.plantuml), abra um `.puml` e use `Alt+D` para a pré-visualização.

**Linha de comando** (Java 21 já é requisito do backend):

```bash
mvn dependency:get -Dartifact=net.sourceforge.plantuml:plantuml:1.2024.8
java -jar ~/.m2/repository/net/sourceforge/plantuml/plantuml/1.2024.8/plantuml-1.2024.8.jar \
  -charset UTF-8 -Playout=smetana -tpng -o out docs/plantuml/*.puml
```

`-Playout=smetana` usa o motor de layout embutido e dispensa a instalação do Graphviz. Troque `-tpng` por `-tsvg` para gerar SVG.
