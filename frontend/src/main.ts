import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import Aura from '@primevue/themes/aura'
import 'primeicons/primeicons.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { ptBrLocale } from './config/primevue-locale-pt-br'

createApp(App)
  .use(router)
  .use(PrimeVue, {
    theme: {
      preset: Aura,
      options: {
        // O tema segue a preferencia do sistema operacional: o seletor
        // .app-dark e aplicado no <html> por App.vue conforme
        // prefers-color-scheme, mantendo o comportamento que a aplicacao ja
        // tinha antes do PrimeVue.
        darkModeSelector: '.app-dark',
      },
    },
    locale: ptBrLocale,
  })
  .mount('#app')
