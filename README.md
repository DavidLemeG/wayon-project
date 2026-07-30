# Sistema de Agendamento de Transferências — Avaliação Prática Java

Sistema para agendar transferências financeiras. O usuário informa
conta de origem, conta de destino, valor e data da transferência; o
sistema calcula a taxa aplicável conforme a distância (em dias) entre a
data de agendamento e a data da transferência, rejeitando o agendamento
quando essa distância cai fora da janela de 0 a 50 dias. O usuário
também pode consultar o extrato de todos os agendamentos cadastrados.

## Sumário

- [Arquitetura](#arquitetura)
- [Stack](#stack)
- [Como rodar localmente](#como-rodar-localmente)
- [Como testar](#como-testar)
- [Decisões técnicas](#decisões-técnicas)
- [Observabilidade](#observabilidade)
- [Suposições assumidas](#suposições-assumidas)
- [O que faria com mais tempo](#o-que-faria-com-mais-tempo)
- [Regras do enunciado](#regras-do-enunciado)

## Arquitetura

```
Vue 3 (Vite, porta 5173) --axios--> Spring Boot REST API (porta 8080) --> H2 (em memória)
```

- `TransferController` expõe `POST /api/transfers` (agendar) e
  `GET /api/transfers` (extrato).
- `TransferSchedulingService` orquestra: bloqueia autotransferência →
  calcula a taxa (`FeeCalculator`) → persiste. O `save()` só é chamado
  depois de toda validação ter sucesso; nenhum agendamento rejeitado é
  persistido.
- `BracketFeeCalculator` (Strategy) calcula a taxa a partir do enum
  `FeeBracket`, que modela as 6 faixas da tabela do enunciado.
- `GlobalExceptionHandler` traduz **toda** falha para um formato único
  de erro (`ApiError`): campo inválido (400), regra de negócio violada
  (422), corpo malformado (400), uso incorreto do protocolo (405 com
  header `Allow`, 415), rota inexistente (404) e erro inesperado (500).

### Estrutura de pacotes do backend (package by feature)

```
com.wayon.transferscheduling/
  domain/transfer/         TransferSchedule (entidade), FeeBracket (enum),
                            Fee (value object), FeeCalculator/BracketFeeCalculator (Strategy)
    exception/              TransferValidationException, InvalidTransferDateException,
                             SameAccountTransferException
  repository/               TransferScheduleRepository
  service/                  TransferSchedulingService
  api/                      TransferController
    dto/                    TransferRequest, TransferResponse
    exception/              ApiError, GlobalExceptionHandler
  common/                   AccountMasker (mascara conta para log)
  config/                   CorsConfig, ClockConfig
```

### Estrutura do frontend

```
frontend/src/
  services/    api.ts (axios), transferService.ts, transferTypes.ts (espelha os DTOs do backend)
  utils/       format.ts (datas e valores em pt-BR)
  router/      /agendar, /extrato e rota coringa para caminho inexistente
  components/  TransferForm.vue, TransferList.vue (+ specs Vitest)
  views/       ScheduleTransferView.vue, StatementView.vue, NotFoundView.vue
  config/      primevue-locale-pt-br.ts (o PrimeVue só embarca inglês)
  test/        mountWithPrimeVue.ts (helper de montagem), setup.ts (stub de matchMedia)
```

## Stack

- **Backend:** Java 11 + Spring Boot 2.7.18 + Maven (`mvnw`)
- **Persistência:** H2 em memória + Spring Data JPA
- **Frontend:** Vue 3 (Composition API) + TypeScript + Vite + PrimeVue 4 (tema Aura) + axios + vue-router 4
- **Testes:** JUnit 5, Mockito, AssertJ, MockMvc, `@SpringBootTest` (backend) · Vitest + Vue Test Utils (frontend)

Justificativas completas de cada escolha nos [ADRs](docs/adr/).

## Como rodar localmente

**Pré-requisitos:** JDK 11, Node.js 18+ (usado aqui: Node 24), `npm`.
Maven não precisa estar instalado à parte — o projeto usa o wrapper
(`mvnw`/`mvnw.cmd`).

### 1. Backend

```bash
cd backend
# JAVA_HOME precisa apontar para uma JDK 11 (o Boot 2.7.18 é a linha
# viável para essa restricao do enunciado, ver ADR 0001).
# Então cheque se você tem nas variáveis de ambiente ou pela IDE que for usada para uso
# apontando a versão correta 
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. Console H2 em
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:transferscheduling`,
usuário `sa`, senha em branco).

### 2. Frontend

```bash
cd frontend
npm install
npm start
```

A aplicação sobe em `http://localhost:5173` (`npm run dev` faz o mesmo).
O backend precisa estar no ar antes.

A URL da API vem da variável `VITE_API_BASE_URL` (definida em
`frontend/.env.development`, com `http://localhost:8080` como padrão no
código). Para servir o front de outro host, basta defini-la no ambiente
onde o build é gerado — não há URL fixa no código de aplicação.

CORS já vem liberado para **qualquer porta em `localhost`**
(`app.cors.allowed-origin-patterns: http://localhost:*`) — não é preciso
nenhuma configuração extra para rodar os dois juntos, e continua
funcionando se o Vite subir em 5174 por a 5173 estar ocupada. Origens
fora de localhost são recusadas. Ver [ADR 0009](docs/adr/0009-cors-configuravel.md).

## Como testar

### Backend

```bash
cd backend
./mvnw test
```

52 testes: fronteiras da tabela de taxas (`BracketFeeCalculatorTest`,
17 casos — cada faixa e as rejeições de data fora da janela/no passado),
orquestração com mocks (`TransferSchedulingServiceTest`, prova que
`repository.save()` nunca roda quando a taxa ou a regra de
autotransferência rejeitam o agendamento), contrato HTTP
(`TransferControllerTest`, `@WebMvcTest` — inclui uso incorreto do
protocolo: verbo não suportado → 405 com `Allow`, `Content-Type` inválido
→ 415), CORS (`CorsConfigTest`, `MockMvc` — preflight autorizado e
recusado), mascaramento de dados sensíveis em log (`AccountMaskerTest`,
prova que a conta completa nunca aparece) e testes de integração ponta a
ponta com H2 real (`TransferSchedulingIntegrationTest`, `@SpringBootTest`
+ `TestRestTemplate`, incluindo o 404 no formato `ApiError`).

### Frontend

```bash
cd frontend
npm run test
```

19 testes (Vitest + Vue Test Utils): validação client-side do
formulário (formato da conta e origem igual ao destino), bloqueio de
envio duplicado enquanto a requisição está em andamento, limpeza da
mensagem de erro ao corrigir um campo, sucesso com breakdown da taxa
exibido, tratamento diferenciado de erro 400 (campo inválido) e 422
(regra de negócio), a listagem do extrato (com estado vazio e de erro) e
a formatação brasileira de datas/valores (`format.spec.ts`, com
regressão nos **dois sentidos** da conversão de data: da API para a tela
e da tela para a API, que são armadilhas de fuso espelhadas).

### Testando a API manualmente

Duas coleções prontas, cobrindo o extrato, uma transferência em cada
faixa da tabela e os 4 corner cases (data fora da janela, data no
passado, autotransferência, campos inválidos):

- [`backend/requests.http`](backend/requests.http) — formato nativo do
  JetBrains HTTP Client (IntelliJ/WebStorm), datas calculadas em tempo
  de execução via pre-request script.
- [`backend/insomnia-collection.json`](backend/insomnia-collection.json) —
  exportação nativa do Insomnia (v4), datas fixas relativas à data da
  exportação (ajustar se rodado depois).

## Decisões técnicas

Registradas como ADRs em [`docs/adr/`](docs/adr/), com contexto,
decisão, consequências e alternativas consideradas para cada uma:

1. [Java 11 + Spring Boot 2.7.18](docs/adr/0001-java11-spring-boot-2-7.md)
2. [Enum `FeeBracket` para as faixas de taxa](docs/adr/0002-fee-bracket-enum.md)
3. [Interface `FeeCalculator` (Strategy Pattern)](docs/adr/0003-strategy-fee-calculator.md)
4. [BigDecimal + arredondamento único no total](docs/adr/0004-bigdecimal-money.md)
5. [H2 em memória](docs/adr/0005-h2-in-memory.md)
6. [Formato de erro customizado, 422 para regra de negócio](docs/adr/0006-custom-error-handling.md)
7. [Package by feature](docs/adr/0007-package-by-feature.md)
8. [Bloqueio de autotransferência](docs/adr/0008-bloqueio-auto-transferencia.md)
9. [CORS configurável no backend](docs/adr/0009-cors-configuravel.md)
10. [Vue 3 + TypeScript + Vite](docs/adr/0010-vue3-typescript-vite.md)
11. [Logging de negócio com mascaramento de contas](docs/adr/0011-logging-mascaramento.md)
12. [PrimeVue 4 (MIT) como biblioteca de componentes](docs/adr/0012-primevue-ui.md)

## Observabilidade

O log conta a história completa de cada requisição — o que entrou, como
a taxa foi calculada e o que foi persistido ou recusado:

```
INFO  c.w.t.api.TransferController      : POST /api/transfers recebido: origem=******2132, destino=******6546, valor=222.00, dataTransferencia=2026-08-03
INFO  c.w.t.service.TransferScheduling… : Taxa calculada: 5 dia(s) entre agendamento e transferencia -> taxa fixa R$ 12.00 + aliquota 0% (R$ 0.00) = total R$ 12.00
INFO  c.w.t.service.TransferScheduling… : Agendamento id=1 criado: origem=******2132, destino=******6546, valor=R$ 222.00, taxa total=R$ 12.00, transferencia em 2026-08-03, agendado em 2026-07-29
WARN  c.w.t.service.TransferScheduling… : Agendamento recusado: conta de origem e destino sao a mesma (conta=******2132)
WARN  c.w.t.a.e.GlobalExceptionHandler  : 422 em /api/transfers: A conta de destino não pode ser a mesma que a conta de origem.
WARN  c.w.t.a.e.GlobalExceptionHandler  : 400 em /api/transfers: campos invalidos [originAccount: Conta de origem deve conter exatamente 10 dígitos]
```

Dois pontos deliberados (detalhados na [ADR 0011](docs/adr/0011-logging-mascaramento.md)):

- **Contas saem mascaradas** (`******2132`): log é agregado, retido por
  muito tempo e lido por quem não teria acesso ao dado — conta completa
  em texto puro ali é vazamento silencioso. Os 4 últimos dígitos bastam
  para correlacionar com o registro no banco.
- **Rejeição de negócio é WARN, não ERROR**: o sistema funcionou como
  deveria ao recusar. ERROR fica reservado para falha real, então um
  alerta em cima de ERROR não dispara por causa de cliente enviando
  data inválida.

Para inspecionar o SQL gerado durante o desenvolvimento:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--logging.level.org.hibernate.SQL=DEBUG
```

## Suposições assumidas

- **Conta de origem igual à de destino é bloqueada** (HTTP 422): o
  enunciado não fala nada sobre isso; a decisão e o raciocínio completo
  estão na [ADR 0008](docs/adr/0008-bloqueio-auto-transferencia.md).
- **Fuso horário fixo em `America/Sao_Paulo`** para determinar "hoje"
  (data de agendamento): configurável via propriedade
  (`app.scheduling.time-zone`), injetado como `Clock` no service.
- **Valor da transferência aceita no máximo 2 casas decimais**
  (`@Digits(fraction = 2)`): coerente com a coluna `NUMERIC(19,2)` — ver
  [ADR 0004](docs/adr/0004-bigdecimal-money.md).

## O que faria com mais tempo

- **Faixas de taxa configuráveis** (tabela no banco, com vigência por
  data, em vez do enum `FeeBracket`): o enum é a escolha certa para o
  escopo atual (6 faixas fixas definidas pelo próprio enunciado), mas em
  um sistema real essas faixas costumam ser ajustadas por
  produto/compliance sem depender de deploy. Evoluir para uma tabela
  exigiria também cache em memória e validação de que as faixas
  cadastradas não se sobrepõem (ver [ADR 0002](docs/adr/0002-fee-bracket-enum.md)).
- **Idempotência no agendamento** (ex.: chave de idempotência enviada
  pelo cliente no header, com uma tabela/registro das requisições já
  processadas): evita que um duplo clique, um retry de rede ou uma
  requisição reenviada pelo front crie dois agendamentos idênticos.
  Não implementado neste desafio pelo prazo e por ser mais trabalhoso
  de encaixar corretamente numa stack Java 11/Spring Boot 2.7 (sem os
  recursos mais modernos de versões atuais do Spring), mas é o tipo de
  proteção que um sistema de transferências real precisaria ter.
- **Métricas (Micrometer + Prometheus)**: hoje a observabilidade é só
  log. O passo natural é expor `/actuator/prometheus` com contadores e
  histogramas do domínio — agendamentos por faixa de taxa, contagem de
  rejeições por motivo (data fora da janela vs. autotransferência),
  latência do `POST /api/transfers`, valor total agendado. Isso responde
  perguntas que log não responde bem ("as rejeições aumentaram esta
  semana?"), e permite alertar sobre tendência em vez de sobre linha
  isolada.
- **Tracing distribuído (OpenTelemetry)**: com um correlation id
  propagado por header e um span por operação, dá para seguir uma
  requisição específica através de múltiplas instâncias e serviços.
  Numa instância só, com log sequencial, ainda é possível acompanhar a
  história lendo o log; com várias réplicas atrás de um load balancer,
  deixa de ser.
- **Containerização (Dockerfile multi-stage + docker-compose)**: hoje
  subir o projeto exige JDK 11 e Node instalados na máquina, nas
  versões certas. Um `Dockerfile` multi-stage para o backend (build
  Maven + runtime JRE slim) e outro para o front (build Vite + nginx
  servindo o estático), amarrados por um `docker-compose.yml` na raiz,
  reduziriam a subida a um `docker compose up` — e eliminariam a classe
  de problema "funciona na minha máquina" com a versão de JDK/Node.
- **Esteira de entrega (CI/CD)**: hoje as duas suítes rodam na máquina
  de quem lembra de rodá-las. Um workflow no GitHub Actions executando
  `mvnw test`, `npm run test` e `npm run build` a cada push tornaria a
  suíte um **portão de merge** em vez de uma rotina manual — e é barato
  de montar, já que os testes existem e passam. O passo seguinte seria
  publicar a imagem (aproveitando o `Dockerfile` do item acima) e
  promover para um ambiente de homologação a cada merge na `main`.
  Provisionamento como código (Terraform) entra junto, mas só depois de
  haver uma arquitetura de destino definida: com H2 em memória e sem
  fila, cache ou cluster, ainda não há infraestrutura a descrever.
- **Paginação do extrato**: `GET /api/transfers` devolve a lista
  inteira. Aceitável no escopo do desafio (H2 em memória, volume
  pequeno), mas não escalaria para um extrato com milhares de
  agendamentos.
- **Autenticação/autorização**: a API hoje não tem nenhuma proteção de
  acesso — qualquer cliente pode agendar transferências em nome de
  qualquer conta.
- **Testes end-to-end** (Cypress/Playwright) cobrindo o fluxo completo
  pelo navegador, além dos testes de componente já existentes no
  front-end.
- **Contas de verdade** (cadastro/validação de existência), em vez de
  aceitar qualquer string de 10 dígitos como conta de origem/destino.

## Regras do enunciado

- ✅ API (Spring Boot) + front-end (VueJs) — Java 11.
- ✅ Agendamento com conta de origem, conta de destino (padrão 10
  dígitos), valor, taxa calculada, data da transferência e data de
  agendamento.
- ✅ Cálculo de taxa conforme a tabela de faixas de dias.
- ✅ Sem faixa aplicável (fora de 0–50 dias) → alerta, sem persistir.
- ✅ Extrato de todos os agendamentos cadastrados.
- ✅ Persistência em banco em memória (H2).
- ✅ Histórico de commits granular (`feat`/`fix`/`test`/`docs`/`refactor`
  separados por decisão).
- ✅ Repositório público no GitHub.
- ✅ README com decisões arquiteturais, versões e instruções de subida.
