# ADR 0010 — Vue 3 + TypeScript + Vite

## Status
Aceito

## Contexto
O enunciado permite VueJs (preferencial) ou Angular. TypeScript foi
escolhido junto com o usuário antes de iniciar o front. Restava decidir
a versão do Vue e a ferramenta de build.

## Decisão
**Vue 3** (Composition API com `<script setup lang="ts">`) via **Vite**
(`npm create vite@latest -- --template vue-ts`), não Vue 2/Vue CLI.

Sem Vuex/Pinia: o escopo é um formulário e uma listagem, cada tela com
seu próprio estado local (`reactive`/`ref`), sem necessidade de estado
compartilhado entre componentes.

## Consequências
- Vue 2 atingiu fim de suporte oficial em dezembro/2023; começar um
  projeto novo nessa versão seria dívida técnica desde o primeiro
  commit.
- Vite é o build tool recomendado pelo próprio time do Vue desde a
  versão 3 (Vue CLI está em modo de manutenção); dev server com HMR
  quase instantâneo, comparado ao webpack do Vue CLI.
- `vue-tsc -b && vite build` type-checa o projeto inteiro (incluindo os
  blocos `<script setup lang="ts">` dos `.vue`) antes de gerar o build
  de produção — build só passa se o TypeScript for válido.
- Se o front crescesse além de duas telas simples, Pinia seria a
  próxima adição natural (é o sucessor oficial do Vuex para Vue 3), mas
  introduzi-lo agora seria abstração sem necessidade real.

## Alternativas consideradas
- **Angular**: opção alternativa citada no enunciado, descartada em
  favor de Vue (preferência do usuário, e opção primária sugerida pelo
  enunciado).
- **Vue 2 + Vue CLI**: descartado — versão e ferramenta de build já
  descontinuadas.
- **Vuex/Pinia desde o início**: rejeitado por escopo — duas telas sem
  estado compartilhado não justificam uma store global.
