# clientes-micro

Serviço separado como **monolito modular**, em dois módulos Maven:

| Módulo | Pacotes | Regra |
|---|---|---|
| [`domain`](domain) | `com.orbitapay.clientes.domain`, `com.orbitapay.clientes.application` | Java puro. O pom não declara **nenhuma** dependência, então nada do Spring compila aqui. |
| [`springframework`](springframework) | `com.orbitapay.clientes.web`, `com.orbitapay.clientes.messaging`, `com.orbitapay.clientes.persistence`, `com.orbitapay.clientes.infrastructure` | Aplicação Spring Boot. Depende de `domain` e implementa as interfaces de repositório e de serviço dele. |

## Build e execução

```bash
mvn install -DskipTests                  # compila domain e springframework
mvn -f springframework spring-boot:run   # sobe o serviço
```

A imagem Docker é gerada pelo [Dockerfile](Dockerfile) a partir desta pasta (`docker compose build clientes` na raiz do repositório).
