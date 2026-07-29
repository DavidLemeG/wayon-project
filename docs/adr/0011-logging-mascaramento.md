# ADR 0011 — Logging de negócio com mascaramento de contas

## Status
Aceito

## Contexto
A aplicação subia, respondia e não registrava nada: sem log, uma
investigação ("por que esse agendamento não apareceu no extrato?", "por
que o cliente recebeu 422?") depende de reproduzir o cenário, e um
comportamento intermitente em produção fica impossível de diagnosticar.

Ao mesmo tempo, logar tudo cegamente cria um problema pior num sistema
financeiro: número de conta é dado sensível, e log costuma ser agregado
num coletor central, retido por meses e acessível a gente que não teria
permissão para consultar o dado no banco.

## Decisão
Log via SLF4J (implementação Logback, já no `spring-boot-starter`), com
mensagens parametrizadas (`log.info("... {}", valor)`, nunca
concatenação de String), cobrindo a jornada de cada requisição:

| Camada | Nível | O que registra |
|---|---|---|
| `TransferController` | INFO | Requisição recebida (contas mascaradas, valor, data) e tamanho do extrato retornado |
| `TransferSchedulingService` | INFO | Taxa calculada (dias, parcela fixa, alíquota, total) e agendamento criado (id, valor, taxa, datas) |
| `TransferSchedulingService` | WARN | Rejeição por conta de origem igual à de destino |
| `GlobalExceptionHandler` | WARN | 422 (regra de negócio), 400 (campo inválido ou corpo malformado) |
| `GlobalExceptionHandler` | ERROR | Falha inesperada, com stack trace |

Números de conta passam por `AccountMasker.mask`, que mantém apenas os
4 últimos dígitos (`1232132132` → `******2132`) — suficiente para
correlacionar com o registro no banco durante uma investigação, sem
expor o dado inteiro.

## Consequências
- O log lido de cima para baixo conta a história completa de uma
  requisição, incluindo o cálculo da taxa (o ponto mais provável de
  dúvida do usuário: "por que a taxa foi essa?").
- **Rejeição de negócio é WARN, não ERROR**: o sistema funcionou
  exatamente como deveria ao recusar. ERROR fica reservado para falha
  de verdade — assim um alerta em cima de ERROR não dispara com
  cliente enviando data inválida.
- `AccountMaskerTest` verifica explicitamente que a conta completa
  nunca aparece na saída — é uma garantia de segurança, não um detalhe
  de formatação, então merece teste próprio.
- `logging.charset.console=UTF-8` é necessário porque as mensagens são
  em português: sem isso, em ambiente cujo charset default não é UTF-8
  (Windows/Cp1252), os acentos saem corrompidos (`n?o pode`,
  `d?gitos`). Verificado na prática, antes e depois, com a aplicação
  rodando.

## Alternativas consideradas
- **Não mascarar as contas**: mais simples e mais conveniente para
  depurar, mas transforma o log num repositório paralelo de dados
  sensíveis, fora dos controles de acesso do banco.
- **Mascarar a conta inteira** (`**********`): elimina qualquer risco,
  mas também elimina a utilidade — sem nenhum dígito não dá para
  correlacionar as linhas de log com um agendamento específico.
- **Log de rejeição em ERROR**: tornaria qualquer alerta baseado em
  ERROR inútil, já que rejeições por regra de negócio são esperadas e
  frequentes.
- **Logar o corpo cru da requisição** (`@RequestBody` completo): pegaria
  campos futuros automaticamente, mas passaria por cima do mascaramento
  — exatamente o tipo de log que vaza dado sem ninguém perceber.
