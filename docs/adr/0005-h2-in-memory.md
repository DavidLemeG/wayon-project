# ADR 0005 — H2 em memória

## Status
Aceito

## Contexto
O enunciado exige explicitamente persistência em banco de dados em
memória, citando H2 como exemplo.

## Decisão
H2 em memória (`jdbc:h2:mem:transferscheduling;DB_CLOSE_DELAY=-1`), com
Spring Data JPA (`spring.jpa.hibernate.ddl-auto=update`, o schema é
criado a partir das entidades) e console H2 habilitado
(`/h2-console`) para inspeção manual durante o desenvolvimento.

`DB_CLOSE_DELAY=-1` mantém o banco vivo enquanto a JVM da aplicação
estiver rodando — sem essa flag, o H2 em memória fecha (e perde todos
os dados) assim que a última conexão do pool é liberada, o que pode
acontecer entre requisições.

## Consequências
- Nenhuma infraestrutura externa necessária para rodar o projeto — só
  `mvnw spring-boot:run`.
- **Dados não sobrevivem a um restart da aplicação** — é o trade-off
  esperado de um banco em memória, coerente com o pedido do enunciado.
- Cada execução de teste `@SpringBootTest`/`@DataJpaTest` sobe uma
  instância H2 isolada, sem necessidade de limpar estado entre suítes.
- `ddl-auto=update` é aceitável para o escopo do desafio (schema não
  muda depois do primeiro boot); não há necessidade de Flyway/Liquibase
  para uma única tabela que nasce e morre com a JVM.

## Alternativas consideradas
- **PostgreSQL/outro banco real via Testcontainers**: mais próximo de
  produção, mas contraria a exigência explícita do enunciado de banco
  em memória, e adicionaria Docker como pré-requisito para rodar o
  projeto localmente sem necessidade real no escopo pedido.
- **`ddl-auto=create-drop`**: recriaria o schema a cada boot; equivalente
  na prática para este projeto (schema nunca é alterado manualmente em
  dev), `update` foi escolhido só por ser o padrão mais comumente visto.
