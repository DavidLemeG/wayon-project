# ADR 0004 — BigDecimal para valores monetários, arredondamento único no total

## Status
Aceito

## Contexto
Valor da transferência, taxa fixa, taxa percentual e taxa total são
dinheiro. Erros de arredondamento binário (`double`/`float`) são
inaceitáveis nesse domínio.

Além do tipo, havia uma decisão mais sutil: a taxa total é
`taxaFixa + (taxaPercentual × valor)`. Arredondar a parcela percentual
para 2 casas decimais e depois somar à taxa fixa (já em 2 casas) pode
divergir, por poucos centavos, de arredondar a soma completa uma única
vez — e essa divergência pode se acumular de forma inconsistente entre
diferentes valores de transferência.

## Decisão
`BigDecimal` em todo o domínio (`TransferSchedule.amount/fixedFee/
percentageFee/totalFee`, mapeados para `NUMERIC(19,2)`; `percentageRate`
como `NUMERIC(19,4)` para acomodar taxas como `0.082`).

Em `BracketFeeCalculator`, o arredondamento (`RoundingMode.HALF_UP`)
acontece **uma única vez**, sobre a soma `fixedFee + (amount × percentageRate)`.
A `percentageFee` exibida no breakdown da resposta é então **derivada**
do total já arredondado (`totalFee - fixedFee`), garantindo que a soma
das partes exibidas sempre bate exatamente com o total.

## Consequências
- Nenhum erro de representação binária de fração decimal.
- `fixedFee + percentageFee == totalFee` é sempre verdade nos dados
  retornados pela API, sem exceções de centavo perdido por
  arredondamento em duas etapas.
- Validação de entrada usa `@DecimalMin("0.01")` em `TransferRequest.amount`
  para rejeitar valores não positivos antes mesmo de chegar ao domínio.
- Comparações de valores monetários em teste usam `isEqualByComparingTo`
  (AssertJ), nunca `equals`, já que `BigDecimal.equals` também compara
  escala (`new BigDecimal("12.0").equals(new BigDecimal("12.00"))` é
  `false`).

## Alternativas consideradas
- **`double`/`float`**: descartado sem ressalvas — representação binária
  imprecisa para frações decimais, inadequado para dinheiro.
- **Arredondar `fixedFee` e `percentageFee` separadamente antes de
  somar**: mais simples de implementar, mas pode gerar um total que não
  bate exatamente com a soma das partes exibidas ao usuário — rejeitado
  por criar uma inconsistência visível no extrato.
- **Inteiro representando centavos** (`long totalFeeInCents`): evita
  cuidado com escala do `BigDecimal`, mas exige conversão manual em toda
  entrada/saída JSON e não é o padrão do ecossistema JPA/H2 para
  `NUMERIC`.
