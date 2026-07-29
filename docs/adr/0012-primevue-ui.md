# ADR 0012 — PrimeVue 4 (MIT) como biblioteca de componentes

## Status
Aceito

## Contexto
A interface usava CSS escrito à mão. Funcionava, mas tinha dois
problemas:

1. **Acessibilidade**: as cores misturavam valores fixos com variáveis de
   tema. No tema escuro, o cabeçalho da tabela ficava com fundo
   `#F9FAFB` (quase branco) fixo e texto `#D1D5DB` herdado do tema —
   contraste de **1,47:1**, quando o mínimo do WCAG AA é 4,5:1.
   Praticamente ilegível, e ninguém percebeu até medir.
2. **Componentes básicos faltando**: campo de data e de moeda eram
   `<input>` nativos, sem calendário localizado nem máscara de moeda; a
   tabela não tinha ordenação nem paginação.

## Decisão
**PrimeVue 4.5.4** com o preset de tema **Aura**, mais `primeicons`.

- Formulário: `Card`, `InputText`, `InputNumber`
  (`mode="currency" currency="BRL" locale="pt-BR"`), `DatePicker`,
  `Message`, `Button`.
- Extrato: `DataTable` + `Column`, com ordenação e paginação.
- Locale pt-BR próprio (`config/primevue-locale-pt-br.ts`): o PrimeVue
  embarca apenas inglês.
- Sem Pinia/Vuex: continua estado local por componente
  (ver [ADR 0010](0010-vue3-typescript-vite.md)).

### Por que a versão 4, e não a 5
O PrimeVue **5 deixou de ser MIT**. Passou a usar a *PrimeUI License*,
comercial, que exige chave de licença mesmo na modalidade gratuita
("Community"), com renovação anual e confirmação de elegibilidade — e
sem a chave a biblioteca emite `[PrimeUI] PrimeUI license is not
configured`.

Isso foi descoberto ao instalar (a v5 é a `latest`) e é inadequado aqui:
o avaliador rodaria o projeto e veria um aviso de licença, e a entrega
carregaria uma dependência que exige registro para uso legítimo. A
**4.5.4 é a última versão MIT** e tem a API equivalente.

## Consequências
- Contraste do cabeçalho da tabela medido no navegador depois da
  migração: **17,72:1** (era 1,47:1) — as cores agora vêm dos design
  tokens do tema (`--p-*`), consistentes em claro e escuro.
- `InputNumber` com `mode="currency"` formata o valor como R$ enquanto
  se digita, e `DatePicker` exibe/edita em `dd/mm/aaaa` — a formatação
  de exibição em `utils/format.ts` continua necessária só para o
  extrato.
- O preset Aura decide o tema por **seletor CSS**, não por
  `@media (prefers-color-scheme)`. `App.vue` passou a aplicar a classe
  `.app-dark` no `<html>` conforme a preferência do sistema — sem isso a
  aplicação ficaria presa no tema claro para quem usa o SO em modo
  escuro.
- Bundle maior (o extrato passou a ~73 kB gzip, contra ~17 kB antes). É
  o preço do `DataTable`; aceitável para uma aplicação interna, e
  atenuado pelo code splitting por rota que já existia.
- Testes precisam montar o plugin do PrimeVue: helper
  `test/mountWithPrimeVue.ts` centraliza isso com a mesma configuração
  de `main.ts`.
- `jsdom` não implementa `window.matchMedia`, usado pelo `Select` do
  paginador e pelo `App.vue` — stub em `test/setup.ts`.

## Armadilha encontrada
`InputNumber` e `DatePicker` são *wrappers*: o atributo `id` fica no
elemento externo, não no `<input>` interno (que recebe um id gerado,
tipo `pv_id_4`). Como os rótulos usavam `<label for="amount">`, a
associação rótulo/campo quebrou — clicar no rótulo não focava o campo e
leitores de tela não anunciavam. O correto nesses componentes é a prop
**`inputId`**. Só apareceu inspecionando o DOM no navegador; visualmente
a tela continuava idêntica.

## Alternativas consideradas
- **Bootstrap 5 (só CSS)**: menor risco e mais familiar, mas não
  resolveria date picker nem máscara de moeda, e o visual ficaria mais
  genérico.
- **Element Plus / Vuetify**: também MIT e completas; PrimeVue foi
  escolhida pela combinação `DataTable` + `InputNumber` com moeda
  brasileira nativa, que cobre exatamente as necessidades deste CRUD.
- **Tailwind CSS**: controle total, mas os componentes continuariam
  sendo construídos à mão — mais tempo para o mesmo resultado.
- **Manter CSS próprio**, apenas corrigindo o contraste: viável e sem
  dependência nova, mas não entregaria ordenação, paginação nem
  calendário localizado.
