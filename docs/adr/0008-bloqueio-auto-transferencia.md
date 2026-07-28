# ADR 0008 — Bloqueio de transferência com conta de origem igual à de destino

## Status
Aceito

## Contexto
O enunciado não menciona o que fazer quando a conta de origem e a conta
de destino são a mesma. Essa é uma lacuna de requisito deliberadamente
deixada em aberto — o próprio enunciado diz que o objetivo é avaliar
"estilo, eficiência, qualidade" além do código em si.

A pergunta foi levada à pessoa responsável pela avaliação, que respondeu
que o critério era meu. Diante disso, e considerando os problemas
práticos que uma autotransferência sem restrição causaria — não altera o
saldo final do cliente, gera um registro de transação sem efeito
prático, e ainda cobraria uma taxa por uma movimentação que não move
dinheiro nenhum entre contas —, decidi adotar o mesmo comportamento
padrão de sistemas de transferência reais (PIX, TED, DOC), que bloqueiam
autotransferência na origem.

## Decisão
`TransferSchedulingService.schedule` rejeita a requisição quando
`originAccount.equals(destinationAccount)`, lançando
`SameAccountTransferException` **antes** de calcular a taxa e antes de
qualquer persistência — mapeada para **422 Unprocessable Entity**
(mesma convenção da [ADR 0006](0006-custom-error-handling.md): erro de
regra de negócio, não de formato de campo), com a mensagem "A conta de
destino não pode ser a mesma que a conta de origem."

## Consequências
- Nenhum agendamento "fantasma" (sem efeito financeiro real) é
  persistido no extrato.
- `TransferSchedulingServiceTest.naoPersisteQuandoContaOrigemIgualContaDestino`
  e `TransferSchedulingIntegrationTest.autoTransferenciaERejeitadaComErroDeNegocio`
  cobrem esse caminho.
- Essa é uma **suposição de negócio assumida conscientemente**, não uma
  omissão: o enunciado não pedia essa validação, mas a ausência de
  qualquer restrição sobre isso foi tratada como uma lacuna a preencher
  com o comportamento mais comum do domínio, e não como "qualquer coisa
  vale".

## Alternativas consideradas
- **Permitir autotransferência sem restrição**: mais fiel à leitura
  literal do enunciado (que não proíbe), mas cria um registro sem
  sentido financeiro no extrato e cobra taxa por uma operação que não
  transfere nada entre contas.
- **Bloquear silenciosamente (não persistir, mas retornar 200/201)**:
  rejeitado — esconderia do usuário por que o agendamento não apareceu
  no extrato; um erro explícito (422) é mais transparente.
