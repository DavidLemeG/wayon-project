<script setup lang="ts"></script>

<template>
  <div class="app">
    <header class="app-header">
      <h1>Agendamento de Transferências</h1>
      <nav>
        <RouterLink to="/agendar">Agendar</RouterLink>
        <RouterLink to="/extrato">Extrato</RouterLink>
      </nav>
    </header>

    <main class="app-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app {
  max-width: 60rem;
  margin: 0 auto;
  padding: 1.5rem;
  font-family:
    system-ui,
    -apple-system,
    sans-serif;
}

.app-header {
  margin-bottom: 1.5rem;
}

nav {
  display: flex;
  gap: 1rem;
}

nav a {
  text-decoration: none;
  color: #2563eb;
}

nav a.router-link-active {
  font-weight: 700;
}
</style>
