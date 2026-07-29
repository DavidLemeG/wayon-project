<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import axios from 'axios'
import { createTransfer } from '../services/transferService'
import type { ApiError, TransferResponse } from '../services/transferTypes'
import { formatCurrency, formatPercent } from '../utils/format'

const emit = defineEmits<{
  created: [transfer: TransferResponse]
}>()

const form = reactive({
  originAccount: '',
  destinationAccount: '',
  amount: '',
  transferDate: '',
})

const submitting = ref(false)
const errorMessage = ref<string | null>(null)
const fieldErrors = ref<string[]>([])
const lastCreated = ref<TransferResponse | null>(null)

const accountPattern = /^\d{10}$/

const clientErrors = computed(() => {
  const errors: string[] = []

  if (form.originAccount && !accountPattern.test(form.originAccount)) {
    errors.push('Conta de origem deve conter exatamente 10 dígitos')
  }
  if (form.destinationAccount && !accountPattern.test(form.destinationAccount)) {
    errors.push('Conta de destino deve conter exatamente 10 dígitos')
  }
  if (form.amount !== '' && Number(form.amount) <= 0) {
    errors.push('Valor da transferência deve ser maior que zero')
  }

  return errors
})

const isFormValid = computed(() => {
  return (
    accountPattern.test(form.originAccount) &&
    accountPattern.test(form.destinationAccount) &&
    Number(form.amount) > 0 &&
    form.transferDate !== '' &&
    clientErrors.value.length === 0
  )
})

async function handleSubmit() {
  if (!isFormValid.value) {
    return
  }

  submitting.value = true
  errorMessage.value = null
  fieldErrors.value = []
  lastCreated.value = null

  try {
    const created = await createTransfer({
      originAccount: form.originAccount,
      destinationAccount: form.destinationAccount,
      amount: Number(form.amount),
      transferDate: form.transferDate,
    })

    lastCreated.value = created
    emit('created', created)
    resetForm()
  } catch (error) {
    handleError(error)
  } finally {
    submitting.value = false
  }
}

function handleError(error: unknown) {
  if (axios.isAxiosError<ApiError>(error) && error.response) {
    const apiError = error.response.data

    if (error.response.status === 400 && apiError.fieldErrors) {
      fieldErrors.value = apiError.fieldErrors
      errorMessage.value = apiError.message
    } else if (error.response.status === 422) {
      // Regra de negocio violada (data fora da janela 0-50 dias, auto-transferencia).
      errorMessage.value = apiError.message
    } else {
      errorMessage.value = apiError.message ?? 'Erro inesperado ao agendar a transferência.'
    }
  } else {
    errorMessage.value = 'Não foi possível conectar à API. Verifique se o backend está no ar.'
  }
}

function resetForm() {
  form.originAccount = ''
  form.destinationAccount = ''
  form.amount = ''
  form.transferDate = ''
}
</script>

<template>
  <form class="transfer-form" @submit.prevent="handleSubmit">
    <h2>Agendar transferência</h2>

    <div class="field">
      <label for="originAccount">Conta de origem</label>
      <input
        id="originAccount"
        v-model="form.originAccount"
        type="text"
        placeholder="XXXXXXXXXX"
        maxlength="10"
        inputmode="numeric"
      />
    </div>

    <div class="field">
      <label for="destinationAccount">Conta de destino</label>
      <input
        id="destinationAccount"
        v-model="form.destinationAccount"
        type="text"
        placeholder="XXXXXXXXXX"
        maxlength="10"
        inputmode="numeric"
      />
    </div>

    <div class="field">
      <label for="amount">Valor (R$)</label>
      <input id="amount" v-model="form.amount" type="number" min="0.01" step="0.01" />
    </div>

    <div class="field">
      <label for="transferDate">Data da transferência</label>
      <input id="transferDate" v-model="form.transferDate" type="date" />
    </div>

    <ul v-if="clientErrors.length" class="errors">
      <li v-for="error in clientErrors" :key="error">{{ error }}</li>
    </ul>

    <button type="submit" :disabled="!isFormValid || submitting">
      {{ submitting ? 'Agendando...' : 'Agendar transferência' }}
    </button>

    <div v-if="errorMessage" class="alert alert-error">
      <p>{{ errorMessage }}</p>
      <ul v-if="fieldErrors.length">
        <li v-for="error in fieldErrors" :key="error">{{ error }}</li>
      </ul>
    </div>

    <div v-if="lastCreated" class="alert alert-success">
      <p>Transferência agendada com sucesso!</p>
      <p>
        Taxa: {{ formatCurrency(lastCreated.fixedFee) }} fixa +
        {{ formatPercent(lastCreated.percentageRate) }} =
        <strong>{{ formatCurrency(lastCreated.totalFee) }}</strong>
      </p>
    </div>
  </form>
</template>

<style scoped>
.transfer-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-width: 24rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.errors,
.alert ul {
  margin: 0;
  padding-left: 1.25rem;
  color: #b91c1c;
}

.alert {
  padding: 0.75rem;
  border-radius: 0.375rem;
}

.alert-error {
  background: #fef2f2;
  color: #b91c1c;
}

.alert-success {
  background: #f0fdf4;
  color: #15803d;
}

button {
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}
</style>
