<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'

const darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)')

/**
 * O preset Aura decide o tema pelo seletor configurado em main.ts
 * (.app-dark no <html>), nao por @media prefers-color-scheme. Sem isso a
 * aplicacao ficaria presa no tema claro para quem usa o sistema em modo
 * escuro.
 */
function applyTheme(prefersDark: boolean) {
  document.documentElement.classList.toggle('app-dark', prefersDark)
}

function onPreferenceChange(event: MediaQueryListEvent) {
  applyTheme(event.matches)
}

onMounted(() => {
  applyTheme(darkModeQuery.matches)
  darkModeQuery.addEventListener('change', onPreferenceChange)
})

onUnmounted(() => darkModeQuery.removeEventListener('change', onPreferenceChange))
</script>

<template>
  <div class="app">
    <header class="app-header">
      <h1>Agendamento de Transferências</h1>
      <nav>
        <RouterLink to="/agendar">
          <i class="pi pi-calendar-plus" aria-hidden="true"></i>
          Agendar
        </RouterLink>
        <RouterLink to="/extrato">
          <i class="pi pi-list" aria-hidden="true"></i>
          Extrato
        </RouterLink>
      </nav>
    </header>

    <main class="app-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app {
  max-width: 72rem;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

.app-header {
  margin-bottom: 2rem;
}

.app-header h1 {
  font-size: 1.75rem;
  margin: 0 0 1rem;
}

nav {
  display: flex;
  gap: 0.5rem;
}

nav a {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: var(--p-border-radius-md, 6px);
  text-decoration: none;
  color: var(--p-text-muted-color);
  transition:
    background-color 0.2s,
    color 0.2s;
}

nav a:hover {
  background: var(--p-content-hover-background);
  color: var(--p-text-color);
}

nav a.router-link-active {
  background: var(--p-primary-color);
  color: var(--p-primary-contrast-color);
  font-weight: 600;
}
</style>
