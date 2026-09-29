# ativos-service

Serviço separado como **monolito modular**, em dois módulos Maven:

| Módulo | Pacotes | Regra |
|---|---|---|
| [`domain`](domain) | `com.orbitapay.ativos.domain`, `com.orbitapay.ativos.application` | Java puro. O pom não declara **nenhuma** dependência, então nada do Spring compila aqui. |
| [`springframework`](springframework) | `com.orbitapay.ativos.adapter`, `com.orbitapay.ativos.infrastructure` | Aplicação Spring Boot. Depende de `domain` e implementa as portas dele. |

## Build e execução

```bash
mvn install -DskipTests                  # compila domain e springframework
mvn -f springframework spring-boot:run   # sobe o serviço
```

A imagem Docker é gerada pelo [Dockerfile](Dockerfile) a partir desta pasta (`docker compose build ativos-service` na raiz do repositório).
