<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import axios from 'axios'
import Button from 'primevue/button'
import Card from 'primevue/card'
import DatePicker from 'primevue/datepicker'
import InputNumber from 'primevue/inputnumber'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import { createTransfer } from '../services/transferService'
import type { ApiError, TransferResponse } from '../services/transferTypes'
import { formatCurrency, formatPercent, toIsoDate } from '../utils/format'

const form = reactive({
  originAccount: '',
  destinationAccount: '',
  amount: null as number | null,
  transferDate: null as Date | null,
})

const submitting = ref(false)
const errorMessage = ref<string | null>(null)
const fieldErrors = ref<string[]>([])
const lastCreated = ref<TransferResponse | null>(null)

const accountPattern = /^\d{10}$/

const originError = computed(() =>
  form.originAccount && !accountPattern.test(form.originAccount)
    ? 'Conta de origem deve conter exatamente 10 dígitos'
    : null,
)

const destinationError = computed(() =>
  form.destinationAccount && !accountPattern.test(form.destinationAccount)
    ? 'Conta de destino deve conter exatamente 10 dígitos'
    : null,
)

/**
 * A regra tambem existe no servidor (422, ver ADR 0008), que continua sendo a
 * fonte da verdade. Aqui e so para o usuario nao preencher o formulario
 * inteiro e so descobrir o problema depois de enviar.
 */
const sameAccountError = computed(() =>
  form.originAccount && form.originAccount === form.destinationAccount
    ? 'A conta de destino não pode ser a mesma que a conta de origem'
    : null,
)

const isFormValid = computed(
  () =>
    accountPattern.test(form.originAccount) &&
    accountPattern.test(form.destinationAccount) &&
    sameAccountError.value === null &&
    form.amount !== null &&
    form.amount > 0 &&
    form.transferDate !== null,
)

// Mensagem de erro do envio anterior nao deve continuar na tela enquanto o
// usuario corrige os campos.
watch(
  () => [form.originAccount, form.destinationAccount, form.amount, form.transferDate],
  () => {
    errorMessage.value = null
    fieldErrors.value = []
  },
)

async function handleSubmit() {
  // A guarda de submitting nao e redundante com o :disabled do botao: o
  // backend nao tem idempotencia, entao um envio duplicado criaria dois
  // agendamentos reais, com duas taxas cobradas.
  if (!isFormValid.value || submitting.value) {
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
      amount: form.amount as number,
      transferDate: toIsoDate(form.transferDate as Date),
    })

    resetForm()
    lastCreated.value = created
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
  form.amount = null
  form.transferDate = null
}
</script>

<template>
  <Card class="transfer-form-card">
    <template #title>Agendar transferência</template>

    <template #content>
      <form class="transfer-form" @submit.prevent="handleSubmit">
        <div class="field">
          <label for="originAccount">Conta de origem</label>
          <InputText
            id="originAccount"
            v-model="form.originAccount"
            placeholder="XXXXXXXXXX"
            maxlength="10"
            inputmode="numeric"
            :invalid="!!originError"
            fluid
          />
          <small v-if="originError" class="field-error">{{ originError }}</small>
        </div>

        <div class="field">
          <label for="destinationAccount">Conta de destino</label>
          <InputText
            id="destinationAccount"
            v-model="form.destinationAccount"
            placeholder="XXXXXXXXXX"
            maxlength="10"
            inputmode="numeric"
            :invalid="!!destinationError || !!sameAccountError"
            fluid
          />
          <small v-if="destinationError" class="field-error">{{ destinationError }}</small>
          <small v-else-if="sameAccountError" class="field-error">{{ sameAccountError }}</small>
        </div>

        <div class="field">
          <label for="amount">Valor da transferência</label>
          <!-- inputId, nao id: InputNumber/DatePicker sao wrappers, e "id" fica
               no elemento externo. Sem inputId o <label for> aponta para um id
               inexistente e a associacao rotulo/campo quebra. -->
          <InputNumber
            input-id="amount"
            v-model="form.amount"
            mode="currency"
            currency="BRL"
            locale="pt-BR"
            :min="0"
            placeholder="R$ 0,00"
            fluid
          />
        </div>

        <div class="field">
          <label for="transferDate">Data da transferência</label>
          <DatePicker
            input-id="transferDate"
            v-model="form.transferDate"
            date-format="dd/mm/yy"
            show-icon
            fluid
          />
        </div>

        <Button
          type="submit"
          :label="submitting ? 'Agendando...' : 'Agendar transferência'"
          icon="pi pi-check"
          :disabled="!isFormValid || submitting"
          :loading="submitting"
        />

        <Message v-if="errorMessage" severity="error" :closable="false">
          <p class="alert-title">{{ errorMessage }}</p>
          <ul v-if="fieldErrors.length" class="alert-list">
            <li v-for="error in fieldErrors" :key="error">{{ error }}</li>
          </ul>
        </Message>

        <Message v-if="lastCreated" severity="success" :closable="false">
          <p class="alert-title">Transferência agendada com sucesso!</p>
          <p>
            Taxa: {{ formatCurrency(lastCreated.fixedFee) }} fixa +
            {{ formatPercent(lastCreated.percentageRate) }} =
            <strong>{{ formatCurrency(lastCreated.totalFee) }}</strong>
          </p>
        </Message>
      </form>
    </template>
  </Card>
</template>

<style scoped>
.transfer-form-card {
  max-width: 30rem;
}

.transfer-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.field label {
  font-weight: 500;
  font-size: 0.875rem;
}

.field-error {
  color: var(--p-red-500);
}

.alert-title {
  margin: 0;
  font-weight: 600;
}

.alert-list {
  margin: 0.5rem 0 0;
  padding-left: 1.25rem;
}
</style>
