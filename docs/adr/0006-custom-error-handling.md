# ADR 0006 — Formato de erro customizado (`ApiError`) e 422 para regra de negócio

## Status
Aceito

## Contexto
A API precisa comunicar dois tipos de erro bem diferentes:

1. Campo mal formado ou ausente (ex.: conta com menos de 10 dígitos,
   valor negativo) — erro de **entrada**.
2. Regra de negócio violada mesmo com campos bem formados (data fora da
   janela de 0–50 dias; conta de origem igual à de destino, ver
   [ADR 0008](0008-bloqueio-auto-transferencia.md)) — erro de
   **domínio**, e o enunciado é explícito: "lançar um alerta sobre o
   erro e não permitir transferência".

Em Spring Boot 3.x, `ProblemDetail` (RFC 7807) resolveria isso de forma
padronizada — mas é um recurso do Spring 6, indisponível no Boot 2.7.18
usado aqui ([ADR 0001](0001-java11-spring-boot-2-7.md)).

## Decisão
`@RestControllerAdvice` (`GlobalExceptionHandler`) com um corpo de erro
próprio (`ApiError`: `timestamp`, `status`, `error`, `message`, `path`,
`fieldErrors`), inspirado na estrutura do `ProblemDetail` sem depender
dele.

- `MethodArgumentNotValidException` (Bean Validation) → **400 Bad
  Request**, com `fieldErrors` listando cada campo inválido.
- `TransferValidationException` (classe base para
  `InvalidTransferDateException` e `SameAccountTransferException`) →
  **422 Unprocessable Entity**, sem `fieldErrors` (o problema não é um
  campo específico, é a combinação de valores).

## Consequências
- Contrato de erro único e previsível para o front-end consumir,
  independentemente de qual validação falhou.
- 422 (não 400) para erro de negócio deixa claro, no próprio código de
  status, que a requisição estava sintaticamente correta mas foi
  rejeitada por uma regra. Aqui o enunciado exige explicitamente que a criação seja
  **impedida**, então é tratado como erro de fato.
- Qualquer nova regra de domínio só precisa estender
  `TransferValidationException` para ganhar automaticamente o mapeamento
  para 422 — não é preciso tocar no `GlobalExceptionHandler` a cada nova
  exceção de negócio (Open/Closed).

## Revisão — herdar de `ResponseEntityExceptionHandler`

A primeira versão usava um `@ExceptionHandler(Exception.class)` como
rede de segurança para "qualquer falha não prevista". Um code review
posterior mostrou que ele capturava demais: as exceções que o **próprio
Spring MVC** lança para sinalizar erro do cliente caíam nele e viravam
**500**. Verificado com a aplicação rodando:

| Requisição | Devolvia | Correto |
|---|---|---|
| `PUT /api/transfers` | 500 | **405** Method Not Allowed |
| `POST` com `Content-Type: text/plain` | 500 | **415** Unsupported Media Type |

Três consequências, todas reais:

1. O cliente era informado de "erro interno" quando o erro era dele.
2. A resposta 405 não trazia o header `Allow`, então não havia como
   descobrir quais métodos a rota aceita.
3. Cada caso gerava um **ERROR com stack trace** no log — exatamente o
   ruído que a [ADR 0011](0011-logging-mascaramento.md) diz que ERROR
   deve evitar. Um alerta baseado em ERROR passaria a disparar por
   causa de um cliente usando o verbo errado.

`GlobalExceptionHandler` passou a estender `ResponseEntityExceptionHandler`,
que já mapeia essas exceções com o status correto; os overrides mantêm o
corpo no formato `ApiError` e repassam os headers montados pelo Spring
(incluindo o `Allow`). O `@ExceptionHandler(Exception.class)` continua
existindo, mas agora só alcança o que de fato não é previsto — e aí
ERROR é o nível certo.

Na mesma revisão, o **404** também saiu do formato padrão do Spring
(que não traz `message`, deixando o front com `undefined`) e passou a
responder `ApiError`, via `spring.mvc.throw-exception-if-no-handler-found`
combinado com `spring.web.resources.add-mappings: false` — o segundo é
necessário porque, com o handler de recursos estáticos ativo, ele atende
qualquer rota não mapeada e a exceção nunca chega a ser lançada.

## Alternativas consideradas
- **`ProblemDetail`/RFC 7807 nativo**: mais padronizado, mas exige Spring
  6/Boot 3+, incompatível com a restrição de Java 11.
- **400 para tudo (campo e negócio)**: mais simples, mas perde a
  distinção semântica entre "você mandou algo mal formado" e "o que você
  mandou é válido, mas a regra de negócio não permite".
