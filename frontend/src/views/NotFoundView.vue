<script setup lang="ts">
import Button from 'primevue/button'
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <section class="not-found">
    <h2>Página não encontrada</h2>
    <p>O endereço acessado não existe nesta aplicação.</p>
    <Button label="Ir para o agendamento" icon="pi pi-arrow-right" @click="router.push('/agendar')" />
  </section>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1rem;
}

.not-found h2 {
  margin: 0;
}

.not-found p {
  color: var(--p-text-muted-color);
}
</style>
