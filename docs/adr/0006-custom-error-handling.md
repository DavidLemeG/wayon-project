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

## Alternativas consideradas
- **`ProblemDetail`/RFC 7807 nativo**: mais padronizado, mas exige Spring
  6/Boot 3+, incompatível com a restrição de Java 11.
- **400 para tudo (campo e negócio)**: mais simples, mas perde a
  distinção semântica entre "você mandou algo mal formado" e "o que você
  mandou é válido, mas a regra de negócio não permite".
