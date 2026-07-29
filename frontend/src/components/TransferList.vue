<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listTransfers } from '../services/transferService'
import type { TransferResponse } from '../services/transferTypes'

const transfers = ref<TransferResponse[]>([])
const loading = ref(true)
const errorMessage = ref<string | null>(null)

defineExpose({ reload })

async function reload() {
  loading.value = true
  errorMessage.value = null

  try {
    transfers.value = await listTransfers()
  } catch {
    errorMessage.value = 'Não foi possível carregar o extrato. Verifique se o backend está no ar.'
  } finally {
    loading.value = false
  }
}

onMounted(reload)
</script>

<template>
  <section class="statement">
    <h2>Extrato de agendamentos</h2>

    <p v-if="loading">Carregando...</p>
    <p v-else-if="errorMessage" class="error">{{ errorMessage }}</p>
    <p v-else-if="transfers.length === 0">Nenhum agendamento cadastrado ainda.</p>

    <table v-else>
      <thead>
        <tr>
          <th>Origem</th>
          <th>Destino</th>
          <th>Valor</th>
          <th>Taxa fixa</th>
          <th>Taxa %</th>
          <th>Taxa total</th>
          <th>Data da transferência</th>
          <th>Data de agendamento</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="transfer in transfers" :key="transfer.id">
          <td>{{ transfer.originAccount }}</td>
          <td>{{ transfer.destinationAccount }}</td>
          <td>R$ {{ transfer.amount.toFixed(2) }}</td>
          <td>R$ {{ transfer.fixedFee.toFixed(2) }}</td>
          <td>{{ (transfer.percentageRate * 100).toFixed(2) }}%</td>
          <td>R$ {{ transfer.totalFee.toFixed(2) }}</td>
          <td>{{ transfer.transferDate }}</td>
          <td>{{ transfer.schedulingDate }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
table {
  border-collapse: collapse;
  width: 100%;
}

th,
td {
  border: 1px solid #e5e7eb;
  padding: 0.5rem 0.75rem;
  text-align: left;
}

th {
  background: #f9fafb;
}

.error {
  color: #b91c1c;
}
</style>
