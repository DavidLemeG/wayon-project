# ADR 0009 — CORS configurável no backend (em vez de proxy no dev server)

## Status
Aceito

## Contexto
O front-end roda no dev server do Vite (`http://localhost:5173`) e a API
em `http://localhost:8080`. São origens diferentes, então o navegador
aplica a same-origin policy: antes de um `POST` com `Content-Type:
application/json`, dispara um preflight `OPTIONS`. Sem CORS liberado, a
chamada é bloqueada **no navegador**, mesmo com a API respondendo
normalmente — um sintoma que aparece só no browser, nunca no `curl`.

Havia duas formas de resolver:

1. **Proxy no Vite** (`server.proxy` no `vite.config.ts`): o dev server
   repassa `/api` para a 8080, então o navegador enxerga tudo como
   mesma origem e CORS nunca entra em cena.
2. **CORS configurado no backend**.

## Decisão
CORS configurado no backend (`CorsConfig`, implementando
`WebMvcConfigurer.addCorsMappings`), restrito a `/api/**`, aos métodos
`GET`/`POST` e ao header `Content-Type` — não `allowedOrigins("*")`.

As origens permitidas vêm da propriedade `app.cors.allowed-origins`
(`application.yml`), com `localhost:5173` (dev server do Vite) e
`localhost:4173` (`vite preview`, build local) por padrão.

## Consequências
- Funciona para qualquer cliente, não só para o front rodando atrás do
  dev server: o build de produção do front, servido de outro domínio,
  continua funcionando — basta ajustar a propriedade, sem recompilar.
- Publicar o front em outro domínio é mudança de configuração
  (variável de ambiente / `--app.cors.allowed-origins=...`), não de código.
- Origem não autorizada recebe **403** no preflight, o que é o
  comportamento correto — a API não fica aberta a qualquer site.
- O proxy do Vite continua sendo uma opção válida **em cima** dessa
  configuração, não em vez dela; as duas abordagens não se excluem.

## Alternativas consideradas
- **Só o proxy do Vite, sem CORS no backend**: resolve o
  desenvolvimento local com uma linha de config no front, mas esconde o
  problema em vez de resolvê-lo — no primeiro deploy real, com o front
  servido de outro domínio, a API voltaria a bloquear as chamadas.
- **`@CrossOrigin` no controller**: espalha a configuração de CORS pela
  camada de API, anotação por anotação, em vez de concentrá-la num
  ponto único configurável.
- **`allowedOrigins("*")`**: liberaria qualquer site a chamar a API pelo
  navegador do usuário. Descartado — é abrir mão de uma proteção real
  em troca de nenhuma conveniência que a propriedade configurável já
  não ofereça.

## Nota sobre o teste
`CorsConfigTest` usa **MockMvc**, não `TestRestTemplate`. O
`TestRestTemplate` usa `HttpURLConnection` por baixo, que descarta
silenciosamente headers restritos — entre eles `Origin` e
`Access-Control-Request-*`. O preflight chegaria ao servidor sem os
headers que o definem como preflight, e o teste passaria (ou falharia)
por engano. Isso foi descoberto na prática: a primeira versão do teste
falhou com `Access-Control-Allow-Origin: null`, enquanto o mesmo
preflight via `curl` respondia corretamente.
