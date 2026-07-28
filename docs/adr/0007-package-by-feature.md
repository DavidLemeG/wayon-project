# ADR 0007 — Package by feature

## Status
Aceito

## Contexto
O backend tem um único domínio (agendamento de transferência), mas
ainda assim se beneficia de uma separação clara entre a regra de
negócio pura e a camada que a expõe como API HTTP.

## Decisão
Pacotes organizados por feature/camada lógica, não por tipo técnico
genérico solto:

```
com.wayon.transferscheduling/
  domain/transfer/          TransferSchedule, FeeBracket, Fee, FeeCalculator, BracketFeeCalculator
    exception/               TransferValidationException, InvalidTransferDateException, SameAccountTransferException
  repository/                TransferScheduleRepository
  service/                   TransferSchedulingService
  api/                       TransferController
    dto/                     TransferRequest, TransferResponse
    exception/               ApiError, GlobalExceptionHandler
```

## Consequências
- `domain/transfer` concentra toda a regra de negócio (entidade, tabela
  de taxas, cálculo, exceções de domínio) sem nenhuma dependência da
  camada HTTP — pode ser testado (`BracketFeeCalculatorTest`) sem subir
  Spring.
- `api/` só conhece DTOs e tradução HTTP; nunca expõe a entidade JPA
  diretamente na resposta (`TransferResponse.from(TransferSchedule)`
  faz essa conversão explicitamente).
- Se o domínio crescesse (ex.: uma segunda feature como "cancelamento de
  agendamento"), o padrão já indica onde adicionar: um novo pacote
  `domain/<feature>` espelhado por `service`/`api` — sem precisar de uma
  reestruturação prévia.

## Alternativas consideradas
- **Package by layer** (`controller/`, `service/`, `repository/`,
  `model/` na raiz, todos misturando features distintas): comum em
  projetos pequenos, mas não escala bem se o domínio crescer, e mistura
  DTOs de features diferentes no mesmo pacote.
- **Módulos Maven separados** (multi-module): overhead desnecessário
  para um domínio único no escopo de um desafio técnico.
