import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import { ptBrLocale } from '../config/primevue-locale-pt-br'

type MountArgs = Parameters<typeof mount>

/**
 * Monta o componente com o plugin do PrimeVue instalado, com a mesma
 * configuracao de main.ts (tema Aura + locale pt-BR). Sem o plugin, os
 * componentes do PrimeVue nao encontram a config injetada e o mount falha.
 */
export function mountWithPrimeVue(component: MountArgs[0], options: MountArgs[1] = {}) {
  return mount(component, {
    ...options,
    global: {
      ...options?.global,
      plugins: [
        [PrimeVue, { theme: { preset: Aura }, locale: ptBrLocale }],
        ...(options?.global?.plugins ?? []),
      ],
    },
  })
}
