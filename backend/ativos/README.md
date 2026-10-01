# ativos-micro

Serviço separado como **monolito modular**, em dois módulos Maven:

| Módulo | Pacotes | Regra |
|---|---|---|
| [`domain`](domain) | `com.orbitapay.ativos.domain`, `com.orbitapay.ativos.application` | Java puro. O pom não declara **nenhuma** dependência, então nada do Spring compila aqui. |
| [`springframework`](springframework) | `com.orbitapay.ativos.web`, `com.orbitapay.ativos.messaging`, `com.orbitapay.ativos.persistence`, `com.orbitapay.ativos.mercado`, `com.orbitapay.ativos.agendamento`, `com.orbitapay.ativos.infrastructure` | Aplicação Spring Boot. Depende de `domain` e implementa as interfaces de repositório e de serviço dele. |

## Build e execução

```bash
mvn install -DskipTests                  # compila domain e springframework
mvn -f springframework spring-boot:run   # sobe o serviço
```

A imagem Docker é gerada pelo [Dockerfile](Dockerfile) a partir desta pasta (`docker compose build ativos` na raiz do repositório).
