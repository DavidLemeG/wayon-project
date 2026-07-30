import { describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import DatePicker from 'primevue/datepicker'
import InputNumber from 'primevue/inputnumber'
import TransferForm from './TransferForm.vue'
import { mountWithPrimeVue } from '../test/mountWithPrimeVue'
import { createTransfer } from '../services/transferService'
import type { ApiError, TransferResponse } from '../services/transferTypes'

vi.mock('../services/transferService', () => ({
  createTransfer: vi.fn(),
}))

const mockedCreateTransfer = vi.mocked(createTransfer)

type Wrapper = ReturnType<typeof mountWithPrimeVue>

/**
 * InputNumber e DatePicker mantem o valor internamente e so publicam via
 * update:modelValue — escrever no <input> deles nao atualiza o v-model do
 * formulario. Emitir o evento e a forma de simular a escolha do usuario.
 */
async function fillForm(
  wrapper: Wrapper,
  values: { origin: string; destination: string; amount: number; date: Date },
) {
  await wrapper.find('#originAccount').setValue(values.origin)
  await wrapper.find('#destinationAccount').setValue(values.destination)
  wrapper.findComponent(InputNumber).vm.$emit('update:modelValue', values.amount)
  wrapper.findComponent(DatePicker).vm.$emit('update:modelValue', values.date)
  await flushPromises()
}

const dadosValidos = {
  origin: '1111111111',
  destination: '2222222222',
  amount: 1000,
  date: new Date(2026, 6, 28),
}

function axiosError(status: number, data: ApiError) {
  return { isAxiosError: true, response: { status, data } }
}

function transferResponseFixture(): TransferResponse {
  return {
    id: 1,
    originAccount: '1111111111',
    destinationAccount: '2222222222',
    amount: 1000,
    fixedFee: 3,
    percentageRate: 0.025,
    percentageFee: 25,
    totalFee: 28,
    transferDate: '2026-07-28',
    schedulingDate: '2026-07-28',
  }
}

describe('TransferForm', () => {
  it('mantem o botao desabilitado enquanto a conta nao tem 10 digitos', async () => {
    const wrapper = mountWithPrimeVue(TransferForm)

    await fillForm(wrapper, { ...dadosValidos, origin: '123' })

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Conta de origem deve conter exatamente 10 dígitos')
    expect(mockedCreateTransfer).not.toHaveBeenCalled()
  })

  it('bloqueia conta de destino igual a de origem antes de enviar', async () => {
    const wrapper = mountWithPrimeVue(TransferForm)

    await fillForm(wrapper, { ...dadosValidos, destination: dadosValidos.origin })

    expect(wrapper.text()).toContain('não pode ser a mesma que a conta de origem')
    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    // A regra tambem existe no servidor (422); aqui e so para o usuario nao
    // preencher tudo e so descobrir depois de enviar.
    expect(mockedCreateTransfer).not.toHaveBeenCalled()
  })

  it('nao envia duas vezes enquanto a primeira requisicao esta em andamento', async () => {
    // O backend nao tem idempotencia: um envio duplicado criaria dois
    // agendamentos reais, com duas taxas cobradas.
    let resolveRequest: (value: TransferResponse) => void = () => {}
    mockedCreateTransfer.mockReturnValueOnce(
      new Promise<TransferResponse>((resolve) => {
        resolveRequest = resolve
      }),
    )

    const wrapper = mountWithPrimeVue(TransferForm)
    await fillForm(wrapper, dadosValidos)

    await wrapper.find('form').trigger('submit.prevent')
    await wrapper.find('form').trigger('submit.prevent')

    expect(mockedCreateTransfer).toHaveBeenCalledTimes(1)

    resolveRequest(transferResponseFixture())
    await flushPromises()
  })

  it('limpa a mensagem de erro quando o usuario corrige um campo', async () => {
    mockedCreateTransfer.mockRejectedValueOnce(
      axiosError(422, {
        timestamp: '2026-07-28T00:00:00Z',
        status: 422,
        error: 'Unprocessable Entity',
        message: 'Não há taxa aplicável para essa data.',
        path: '/api/transfers',
        fieldErrors: null,
      }),
    )

    const wrapper = mountWithPrimeVue(TransferForm)
    await fillForm(wrapper, dadosValidos)
    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('Não há taxa aplicável')

    await wrapper.find('#originAccount').setValue('9999999999')
    await flushPromises()

    expect(wrapper.text()).not.toContain('Não há taxa aplicável')
  })

  it('submete e mostra o breakdown da taxa em caso de sucesso', async () => {
    const created: TransferResponse = {
      id: 1,
      originAccount: '1111111111',
      destinationAccount: '2222222222',
      amount: 1000,
      fixedFee: 3,
      percentageRate: 0.025,
      percentageFee: 25,
      totalFee: 28,
      transferDate: '2026-07-28',
      schedulingDate: '2026-07-28',
    }
    mockedCreateTransfer.mockResolvedValueOnce(created)

    const wrapper = mountWithPrimeVue(TransferForm)
    await fillForm(wrapper, dadosValidos)

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    // A data escolhida no DatePicker (Date local) precisa chegar na API como
    // yyyy-MM-dd do MESMO dia, sem deslocamento de fuso.
    expect(mockedCreateTransfer).toHaveBeenCalledWith({
      originAccount: '1111111111',
      destinationAccount: '2222222222',
      amount: 1000,
      transferDate: '2026-07-28',
    })

    expect(wrapper.text()).toContain('Transferência agendada com sucesso!')
    // Intl usa espaco nao-quebravel entre "R$" e o numero, escapado como
    // \u00A0 de proposito (o caractere literal e indistinguivel de um espaco
    // comum no editor, e o replace passa a nao fazer nada sem ninguem notar).
    const text = wrapper.text().replace(/\u00A0/g, ' ')
    expect(text).toContain('R$ 28,00')
    expect(text).toContain('2,50%')
  })

  it('exibe os fieldErrors quando a API responde 400', async () => {
    mockedCreateTransfer.mockRejectedValueOnce(
      axiosError(400, {
        timestamp: '2026-07-28T00:00:00Z',
        status: 400,
        error: 'Bad Request',
        message: 'Erro de validação nos campos enviados.',
        path: '/api/transfers',
        fieldErrors: ['amount: Valor da transferência deve ter no máximo 2 casas decimais'],
      }),
    )

    const wrapper = mountWithPrimeVue(TransferForm)
    await fillForm(wrapper, dadosValidos)

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('máximo 2 casas decimais')
  })

  it('exibe a mensagem de regra de negocio quando a API responde 422', async () => {
    mockedCreateTransfer.mockRejectedValueOnce(
      axiosError(422, {
        timestamp: '2026-07-28T00:00:00Z',
        status: 422,
        error: 'Unprocessable Entity',
        message: 'A conta de destino não pode ser a mesma que a conta de origem.',
        path: '/api/transfers',
        fieldErrors: null,
      }),
    )

    const wrapper = mountWithPrimeVue(TransferForm)
    await fillForm(wrapper, dadosValidos)

    await wrapper.find('form').trigger('submit.prevent')
    await flushPromises()

    expect(wrapper.text()).toContain('não pode ser a mesma que a conta de origem')
  })
})
