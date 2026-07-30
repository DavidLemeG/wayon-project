# Front-end — Agendamento de Transferências

Interface em Vue 3 + TypeScript para agendar transferências financeiras e
consultar o extrato. Consome a API Spring Boot que fica em
[`../backend`](../backend).

Documentação completa (arquitetura, decisões técnicas, como rodar tudo
junto) no [README da raiz](../README.md).

## Comandos

```bash
npm install
npm start        # dev server em http://localhost:5173 (npm run dev faz o mesmo)
npm run build    # type-check (vue-tsc) + build de produção em dist/
npm run test     # testes de componente e de formatação (Vitest)
```

O backend precisa estar rodando em `http://localhost:8080` antes de
subir o front.

## Configuração

| Variável | Padrão | Para que serve |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | URL base da API. Definida em `.env.development`; para servir o front de outro host, defina-a no ambiente onde o build é gerado. |

## Estrutura

```
src/
  services/    api.ts (axios), transferService.ts, transferTypes.ts (espelha os DTOs do backend)
  utils/       format.ts (datas e valores em pt-BR)
  router/      /agendar, /extrato e rota coringa para caminho inexistente
  components/  TransferForm.vue, TransferList.vue (+ specs)
  views/       ScheduleTransferView.vue, StatementView.vue, NotFoundView.vue
  config/      locale pt-BR do PrimeVue
  test/        helper de montagem com o plugin do PrimeVue + stub de matchMedia
```
