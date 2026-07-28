# Sistema de Agendamento de Transferências — Wayon (Avaliação Prática Java)

> 🚧 Em desenvolvimento. Este README será completado ao final do processo,
> junto com as [ADRs](docs/adr/) de cada decisão técnica.

Sistema para agendar transferências financeiras, calculando a taxa
aplicável conforme a data de transferência, e consultar o extrato de
todos os agendamentos.

## Stack

- Backend: Java 11 + Spring Boot 2.7.18 + Maven
- Persistência: H2 (banco em memória)
- Frontend: VueJs 3 + TypeScript (a iniciar após o backend)

## Decisões técnicas

Ver [`docs/adr/`](docs/adr/).

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
