# Architecture Decision Records

Registro das decisões técnicas relevantes do projeto, formato
[ADR](https://github.com/joelparkerhenderson/architecture-decision-record).

| ADR | Decisão |
|---|---|
| [0001](0001-java11-spring-boot-2-7.md) | Java 11 + Spring Boot 2.7.18 |
| [0002](0002-fee-bracket-enum.md) | Enum `FeeBracket` para as faixas de taxa |
| [0003](0003-strategy-fee-calculator.md) | Interface `FeeCalculator` (Strategy Pattern) |
| [0004](0004-bigdecimal-money.md) | BigDecimal + arredondamento único no total |
| [0005](0005-h2-in-memory.md) | H2 em memória |
| [0006](0006-custom-error-handling.md) | Formato de erro customizado, 422 para regra de negócio |
| [0007](0007-package-by-feature.md) | Package by feature |
| [0008](0008-bloqueio-auto-transferencia.md) | Bloqueio de autotransferência (origem == destino) |
| [0009](0009-cors-configuravel.md) | CORS configurável no backend (vs proxy no dev server) |
| [0010](0010-vue3-typescript-vite.md) | Vue 3 + TypeScript + Vite |
