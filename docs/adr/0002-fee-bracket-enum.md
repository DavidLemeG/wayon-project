# ADR 0002 — Enum `FeeBracket` para as faixas de taxa

## Status
Aceito

## Contexto
O enunciado define 6 faixas de dias entre agendamento e transferência,
cada uma com uma taxa fixa e uma taxa percentual. O conjunto é fechado e
conhecido em tempo de compilação (não vem de configuração externa nem
precisa ser editado por um usuário de negócio).

Em Java 17+ isso seria natural como uma lista de `record`s. Java 11 não
tem `record` (recurso de Java 16+), então era preciso outra forma de
modelar um conjunto fechado e imutável de valores.

## Decisão
Enum `FeeBracket` com `minDays`, `maxDays`, `fixedFee` (`BigDecimal`) e
`percentageRate` (`BigDecimal`) como campos de cada constante, e um
método estático `Optional<FeeBracket> forDays(long days)` que percorre
`values()` e retorna a primeira faixa cujo intervalo contém `days`.

## Consequências
- Retornar `Optional` (em vez de lançar exceção dentro do próprio enum)
  mantém o enum livre de conhecimento sobre o que fazer quando não há
  faixa aplicável — quem decide isso é `BracketFeeCalculator`, que lança
  `InvalidTransferDateException`. `FeeBracket` só descreve dados, não
  political de erro.
- Nenhuma faixa cobre dias negativos, então uma data de transferência no
  passado já cai naturalmente em "nenhuma faixa encontrada", sem
  precisar de uma verificação `if (days < 0)` separada.
- `BracketFeeCalculatorTest` cobre as 6 faixas e as duas fronteiras mais
  arriscadas de inverter (dia 10 → R$12 fixo/0%, dia 11 → R$0 fixo/8,2%).

## Alternativas consideradas
- **Lista de `record`s**: mais idiomático em Java 16+, mas indisponível
  em Java 11.
- **Tabela em banco de dados**: permitiria alterar as faixas sem
  recompilar, mas o enunciado não pede isso e adicionaria uma tabela e
  uma migração só para 6 linhas que nunca mudam no escopo do desafio.
  Em um sistema real, faixas de taxa costumam ser ajustadas por
  produto/compliance sem depender de deploy — vale como evolução futura
  (ver "o que faria com mais tempo" no README), não como decisão a
  tomar agora.
- **Classe `FeeBracket` comum (não enum) com lista estática de
  instâncias**: equivalente em poder de expressão ao enum, mas o enum já
  dá `values()`, `switch` e comparação por identidade de graça.
