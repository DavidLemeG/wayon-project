<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import { listTransfers } from '../services/transferService'
import type { TransferResponse } from '../services/transferTypes'
import { formatCurrency, formatDate, formatPercent } from '../utils/format'

const transfers = ref<TransferResponse[]>([])
const loading = ref(true)
const errorMessage = ref<string | null>(null)

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

    <div v-if="loading" class="statement-loading">
      <ProgressSpinner style="width: 2.5rem; height: 2.5rem" />
    </div>

    <Message v-else-if="errorMessage" severity="error" :closable="false">
      {{ errorMessage }}
    </Message>

    <DataTable
      v-else
      :value="transfers"
      data-key="id"
      paginator
      :rows="10"
      :rows-per-page-options="[10, 25, 50]"
      sort-field="id"
      :sort-order="-1"
      striped-rows
      removable-sort
      current-page-report-template="{first}-{last} de {totalRecords} agendamentos"
      paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
    >
      <template #empty>
        <p class="statement-empty">Nenhum agendamento cadastrado ainda.</p>
      </template>

      <Column field="originAccount" header="Origem" sortable />
      <Column field="destinationAccount" header="Destino" sortable />

      <Column field="amount" header="Valor" sortable>
        <template #body="{ data }">{{ formatCurrency(data.amount) }}</template>
      </Column>

      <Column field="fixedFee" header="Taxa fixa">
        <template #body="{ data }">{{ formatCurrency(data.fixedFee) }}</template>
      </Column>

      <Column field="percentageRate" header="Taxa %">
        <template #body="{ data }">{{ formatPercent(data.percentageRate) }}</template>
      </Column>

      <Column field="totalFee" header="Taxa total" sortable>
        <template #body="{ data }">
          <strong>{{ formatCurrency(data.totalFee) }}</strong>
        </template>
      </Column>

      <Column field="transferDate" header="Data da transferência" sortable>
        <template #body="{ data }">{{ formatDate(data.transferDate) }}</template>
      </Column>

      <Column field="schedulingDate" header="Data de agendamento" sortable>
        <template #body="{ data }">{{ formatDate(data.schedulingDate) }}</template>
      </Column>
    </DataTable>
  </section>
</template>

<style scoped>
.statement h2 {
  margin: 0 0 1rem;
}

.statement-loading {
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.statement-empty {
  text-align: center;
  color: var(--p-text-muted-color);
  margin: 0;
}
</style>
