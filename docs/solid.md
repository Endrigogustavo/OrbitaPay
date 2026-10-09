# SOLID no OrbitaPay

Este documento mostra **onde cada princípio SOLID aparece no código**, com exemplos reais do projeto. Todos os 9 serviços seguem o mesmo padrão, então os exemplos valem para qualquer um deles.

## Organização que vale para todo serviço

```
<servico>/
├── domain/            Java puro (sem Spring): regras de negócio
│   ├── domain/        entidades, eventos, exceções e interfaces dos repositórios
│   └── application/   casos de uso (um por classe) e interfaces de serviços técnicos
└── springframework/   Spring Boot: controllers, listeners, MongoDB, RabbitMQ e configuração
```

O módulo `domain` **não tem nenhuma dependência** no `pom.xml`. Se alguém tentar usar Spring, MongoDB ou RabbitMQ ali, o código nem compila.

## S — Responsabilidade Única (Single Responsibility)

> Uma classe deve ter um único motivo para mudar.

**Cada caso de uso é uma classe com um único método público** (quase sempre `executar`), que faz uma única ação de negócio.

| Serviço | Casos de uso (uma classe para cada) |
|---|---|
| Contas | `AbrirConta`, `Sacar`, `CreditarDeposito`, `DebitarCompra`, `CreditarVenda`, `AtualizarNomeDoTitular`, `AlterarSituacaoDoTitular`, `EncerrarConta` |
| Negociação | `EnviarOrdem`, `ConfirmarCompra`, `CancelarCompra`, `ConfirmarVenda`, `CancelarVenda`, `RegistrarAtivo`, `RetirarAtivo`, `AtualizarCotacoes`, `RegistrarInvestidor`, `RemoverInvestidor` |
| Carteira | `ReservarAcoesParaVenda`, `LiquidarCompra`, `LiquidarVenda`, `CancelarReservaDeVenda`, `EncerrarCarteira`, `RegistrarAtivoCotado`, `RemoverAtivoCotado`, `AtualizarCotacoes` |
| Clientes | `CadastrarCliente`, `AtualizarDadosDoCliente`, `BloquearCliente`, `BloquearPorExcessoDeTentativas`, `DesbloquearPeloCliente`, `DesbloquearPeloGerente`, `RemoverCliente` |
| Auth | `RegistrarCredencial`, `AutenticarCliente`, `AutenticarGerente`, `AssinarOperacao`, `AlterarPin`, `ConferenciaDePin`, `AtualizarEmailDaCredencial`, `ZerarTentativasDePin`, `RemoverCredencial` |

As classes `Consultar...` (`ConsultarContas`, `ConsultarOrdens`, `ConsultarAtivos`…) reúnem **apenas leituras** de um mesmo assunto. A única responsabilidade delas é responder consultas, sem alterar nada.

As demais camadas também têm uma responsabilidade cada:

| Classe | Única responsabilidade |
|---|---|
| `ContaController` | traduzir HTTP ↔ caso de uso |
| `OrdemListener` | traduzir mensagem do RabbitMQ ↔ caso de uso |
| `MongoContaRepository` | traduzir objeto de domínio ↔ documento do MongoDB |
| `TravaPessimistaMongo` | travar e liberar um documento no MongoDB |
| `RabbitPublicadorDeEventosDeConta` | transformar um evento de domínio em mensagem do RabbitMQ |
| `Conta` (entidade) | regras do saldo: sacar, debitar, creditar |

## O — Aberto/Fechado (Open/Closed)

> Aberto para extensão, fechado para modificação.

- **Regras de acesso do gateway** ([`MapaDeAcesso`](../backend/gateway/domain/src/main/java/com/orbitapay/gateway/application/MapaDeAcesso.java)): as rotas são uma **tabela de `RegraDeAcesso`** (método, caminho, nível). Para proteger uma rota nova, basta **acrescentar uma linha** na tabela. O código que decide (`nivel`) não muda.

  ```java
  new RegraDeAcesso("POST", "/api/ordens", CLIENTE_COM_ASSINATURA),
  new RegraDeAcesso("GET", "/api/ordens/**", CLIENTE),
  ```

- **Provedores de pagamento** ([`FachadaDeProvedores`](../backend/pagamentos/springframework/src/main/java/com/orbitapay/pagamentos/acl/FachadaDeProvedores.java)): cada provedor (Pix, boleto, TED) é um `AdaptadorDeProvedor`. Para aceitar um novo meio de pagamento, basta **criar um novo adaptador**: o Spring injeta a lista de adaptadores na fachada, que não é alterada.

## L — Substituição de Liskov (Liskov Substitution)

> Uma implementação deve poder substituir a interface sem quebrar quem a usa.

- O caso de uso `Sacar` recebe uma `TravaDeConta`. Em produção, quem chega é o `MongoContaRepository`. Num teste, poderia ser uma implementação em memória, e o `Sacar` continuaria funcionando igual.
- Os testes de Relatórios fazem exatamente isso: `RelatoriosTest` usa `FatosEmMemoria` e `PerfisEmMemoria` no lugar dos repositórios do MongoDB.
- `AdaptadorPix`, `AdaptadorBoleto` e `AdaptadorTed` são trocáveis entre si para a fachada: todos respeitam o mesmo contrato `AdaptadorDeProvedor`.

## I — Segregação de Interfaces (Interface Segregation)

> Ninguém deve depender de métodos que não usa.

A **trava pessimista** tem uma interface própria, separada do repositório de consultas:

| Interface | Métodos | Quem usa |
|---|---|---|
| `ContaRepository` | `buscarPorCliente`, `listar`, `inserir`, `existePorCliente`, `removerPorCliente` | `AbrirConta`, `ConsultarContas`, `EncerrarConta`, `AtualizarNomeDoTitular`, `AlterarSituacaoDoTitular` |
| `TravaDeConta` | `travar`, `salvarELiberar`, `liberar` | `Sacar`, `CreditarDeposito`, `DebitarCompra`, `CreditarVenda` |

O mesmo vale para `AtivoNegociavelRepository` / `TravaDeAtivo` (Negociação) e `CarteiraRepository` / `TravaDeCarteira` (Carteira). A classe do MongoDB implementa as duas interfaces, mas cada caso de uso só enxerga a que precisa.

Os serviços técnicos também são interfaces pequenas, com um método cada: `PublicadorDeEventosDeConta.publicar`, `CatalogoDeAtivos.consultar`, `CodificadorDePin`, `EmissorDeToken`.

## D — Inversão de Dependência (Dependency Inversion)

> As regras de negócio dependem de abstrações, não de detalhes de tecnologia.

```
domain (Java puro)                         springframework (detalhes)
──────────────────                         ──────────────────────────
Sacar ──usa──▶ TravaDeConta (interface) ◀──implementa── MongoContaRepository
DebitarCompra ──usa──▶ PublicadorDeEventosDeConta ◀──implementa── RabbitPublicadorDeEventosDeConta
CadastrarCliente ──usa──▶ RegistroDeCredencial ◀──implementa── RabbitRegistroDeCredencial
```

- As interfaces ficam no `domain`; as implementações com MongoDB e RabbitMQ ficam no `springframework`.
- Quem liga uma coisa à outra é a configuração [`CasosDeUsoConfig`](../backend/contas/springframework/src/main/java/com/orbitapay/contas/infrastructure/config/CasosDeUsoConfig.java), que cria cada caso de uso passando as implementações no construtor (injeção de dependência).
- Trocar o MongoDB por outro banco exigiria **só novas implementações** no `springframework`. Nenhuma regra de negócio mudaria.

## SOLID no app mobile

O app segue a mesma ideia de responsabilidade única: as ações que o usuário dispara ficam separadas por assunto em [`mobile/src/context/operacoes/`](../mobile/src/context/operacoes):

| Arquivo | Responsabilidade |
|---|---|
| `banco.ts` | depósito e saque |
| `negociacao.ts` | compra e venda de ações |
| `gerencia.ts` | backoffice do gerente (clientes e ativos) |
| `perfil.ts` | PIN, bloqueio e encerramento da própria conta |
| `OperacoesContext.tsx` | só o estado da tela (folha aberta, teclado, erros) e a junção dos módulos acima |

Todo acesso ao backend passa por [`integration/orbitaApi.ts`](../mobile/src/integration/orbitaApi.ts): as telas não sabem montar URLs nem cabeçalhos.
