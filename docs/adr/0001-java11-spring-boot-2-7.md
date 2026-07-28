# ADR 0001 — Java 11 + Spring Boot 2.7.18

## Status
Aceito

## Contexto
O enunciado do desafio exige explicitamente **Java 11**. O Spring
Initializr (`start.spring.io`) hoje só gera projetos para Spring Boot
>= 4.0, então o scaffold do `pom.xml` foi montado manualmente a partir
do `spring-boot-starter-parent`.

Spring Boot 3.x e superiores exigem Java 17 como baseline (namespace
`jakarta.*`), o que é incompatível com a restrição de Java 11. A última
linha do Spring Boot que suporta Java 8/11 é a **2.7.x**, com suporte
open-source encerrado em novembro/2023 — ou seja, é uma linha legada,
mas é a única compatível com o requisito do enunciado.

## Decisão
Java 11 (LTS) com Spring Boot **2.7.18** (última patch da linha 2.7) e
Maven como build tool (`mvnw`/`mvnw.cmd` gerados via
`maven-wrapper-plugin`, wrapper Maven 3.9.6).

O ambiente de desenvolvimento usa a distribuição Eclipse Temurin 11.0.31,
via `JAVA_HOME` apontando para essa JDK ao rodar `mvnw` neste projeto.

## Consequências
- Namespace **`javax.*`** em todo o projeto (`javax.persistence`,
  `javax.validation`), não `jakarta.*` — atenção ao copiar exemplos da
  documentação atual do Spring, que já é toda Boot 3.x/Jakarta.
- Sem `record` (Java 16+) no código de produção — tipos imutáveis usam
  classes normais ou enum com construtor.
- Sem `ProblemDetail` (Spring 6), erro de API precisa de formato
  customizado (ver ADR futura sobre tratamento de erro).
- Build validado rodando `mvnw clean install` com `JAVA_HOME` apontando
  para a JDK 11, gerando `transferscheduling-0.0.1-SNAPSHOT.jar`.

## Alternativas consideradas
- **Spring Boot 3.x/4.x com Java 17+**: rejeitado — o enunciado exige
  Java 11 de forma explícita, não é uma escolha de estilo.
- **Gradle** como build tool: descartado a favor de Maven, mais comum
  em ambientes corporativos Java e já usado no case anterior (Itaú).
