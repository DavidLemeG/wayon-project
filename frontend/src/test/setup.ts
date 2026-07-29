/**
 * jsdom nao implementa window.matchMedia, e componentes do PrimeVue (o Select
 * do paginador, por exemplo) e o App.vue usam para reagir ao tema do sistema.
 * Sem este stub o mount quebra com "matchMedia is not a function".
 */
if (!window.matchMedia) {
  window.matchMedia = (query: string) =>
    ({
      matches: false,
      media: query,
      onchange: null,
      addEventListener: () => {},
      removeEventListener: () => {},
      addListener: () => {},
      removeListener: () => {},
      dispatchEvent: () => false,
    }) as unknown as MediaQueryList
}
