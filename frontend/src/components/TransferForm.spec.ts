import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import TransferForm from './TransferForm.vue'
import { createTransfer } from '../services/transferService'
import type { ApiError, TransferResponse } from '../services/transferTypes'

vi.mock('../services/transferService', () => ({
  createTransfer: vi.fn(),
}))

const mockedCreateTransfer = vi.mocked(createTransfer)

async function fillForm(
  wrapper: ReturnType<typeof mount>,
  values: { origin: string; destination: string; amount: string; date: string },
) {
  await wrapper.find('#originAccount').setValue(values.origin)
  await wrapper.find('#destinationAccount').setValue(values.destination)
  await wrapper.find('#amount').setValue(values.amount)
  await wrapper.find('#transferDate').setValue(values.date)
}

function axiosError(status: number, data: ApiError) {
  return { isAxiosError: true, response: { status, data } }
}

describe('TransferForm', () => {
  it('mantem o botao desabilitado enquanto a conta nao tem 10 digitos', async () => {
    const wrapper = mount(TransferForm)

    await fillForm(wrapper, {
      origin: '123',
      destination: '2222222222',
      amount: '100',
      date: '2026-08-12',
    })

    expect(wrapper.find('button[type="submit"]').attributes('disabled')).toBeDefined()
    expect(wrapper.text()).toContain('Conta de origem deve conter exatamente 10 dígitos')
    expect(mockedCreateTransfer).not.toHaveBeenCalled()
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

    const wrapper = mount(TransferForm)
    await fillForm(wrapper, {
      origin: '1111111111',
      destination: '2222222222',
      amount: '1000',
      date: '2026-07-28',
    })

    await wrapper.find('form').trigger('submit.prevent')
    await wrapper.vm.$nextTick()
    await Promise.resolve()
    await wrapper.vm.$nextTick()

    expect(mockedCreateTransfer).toHaveBeenCalledWith({
      originAccount: '1111111111',
      destinationAccount: '2222222222',
      amount: 1000,
      transferDate: '2026-07-28',
    })
    expect(wrapper.text()).toContain('Transferência agendada com sucesso!')
    // Intl usa espaco nao-quebravel (U+00A0) entre "R$" e o numero.
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

    const wrapper = mount(TransferForm)
    await fillForm(wrapper, {
      origin: '1111111111',
      destination: '2222222222',
      amount: '1000',
      date: '2026-07-28',
    })

    await wrapper.find('form').trigger('submit.prevent')
    await Promise.resolve()
    await wrapper.vm.$nextTick()

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

    const wrapper = mount(TransferForm)
    await fillForm(wrapper, {
      origin: '1111111111',
      destination: '2222222222',
      amount: '1000',
      date: '2026-07-28',
    })

    await wrapper.find('form').trigger('submit.prevent')
    await Promise.resolve()
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('não pode ser a mesma que a conta de origem')
  })
})
