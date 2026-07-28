# ADR 0003 — Interface `FeeCalculator` (Strategy Pattern)

## Status
Aceito

## Contexto
`TransferSchedulingService` precisa calcular a taxa de uma transferência
sem saber os detalhes de como isso é feito.

## Decisão
`FeeCalculator` é uma interface (`Fee calculate(LocalDate schedulingDate,
LocalDate transferDate, BigDecimal amount)`), implementada por
`BracketFeeCalculator` (bean Spring `@Component`). `TransferSchedulingService`
recebe um `FeeCalculator` via injeção por construtor e depende só da
interface — nunca da implementação concreta.

Isso é o **Strategy Pattern**: o algoritmo de cálculo de taxa é
intercambiável sem alterar o service que o usa. Também é uma aplicação
direta de **DIP** (Dependency Inversion Principle) — o módulo de mais
alto nível (`TransferSchedulingService`) depende de uma abstração, não
de um detalhe de implementação.

## Consequências
- `TransferSchedulingService` pode ser testado com um `FeeCalculator`
  mockado (`TransferSchedulingServiceTest`), sem precisar montar a
  tabela real de faixas — teste rápido e focado só na orquestração.
- `BracketFeeCalculator` pode ser testado isoladamente
  (`BracketFeeCalculatorTest`), sem subir o contexto Spring.
- Se um dia surgir uma segunda forma de calcular taxa (ex.: taxa
  promocional para determinadas contas), basta criar uma nova
  implementação de `FeeCalculator` e trocar o bean ativo — o service não
  muda (**Open/Closed Principle**).
- `BracketFeeCalculator` é um bean Spring gerenciado (`@Component`), não
  um POJO isolado do framework: não há necessidade de mantê-lo "puro"
  para ser testável — um bean sem estado e sem dependências continua
  perfeitamente testável com `new BracketFeeCalculator()` direto, como
  feito em `BracketFeeCalculatorTest`.

## Alternativas consideradas
- **Método estático em `FeeBracket` ou classe utilitária estática**: mais
  simples, mas acopla o service à implementação concreta e impede
  substituição/mocking limpo em teste — abre mão do Strategy Pattern.
- **`TransferSchedulingService` chamando `FeeBracket.forDays` diretamente**:
  misturaria a orquestração (agendar, persistir) com a regra de cálculo
  de taxa na mesma classe, violando **SRP**.
