import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import TransferList from './TransferList.vue'
import { listTransfers } from '../services/transferService'
import type { TransferResponse } from '../services/transferTypes'

vi.mock('../services/transferService', () => ({
  listTransfers: vi.fn(),
}))

const mockedListTransfers = vi.mocked(listTransfers)

const sampleTransfer: TransferResponse = {
  id: 1,
  originAccount: '1111111111',
  destinationAccount: '2222222222',
  amount: 1000,
  fixedFee: 0,
  percentageRate: 0.082,
  percentageFee: 82,
  totalFee: 82,
  transferDate: '2026-08-12',
  schedulingDate: '2026-07-28',
}

describe('TransferList', () => {
  it('renderiza uma linha por agendamento retornado pelo servico', async () => {
    mockedListTransfers.mockResolvedValueOnce([sampleTransfer])

    const wrapper = mount(TransferList)
    await flushPromises()

    const row = wrapper.find('tbody tr')
    expect(row.text()).toContain('1111111111')
    expect(row.text()).toContain('2222222222')
    expect(row.text()).toContain('R$ 1000.00')
    expect(row.text()).toContain('8.20%')
    expect(row.text()).toContain('R$ 82.00')
  })

  it('mostra mensagem de vazio quando nao ha agendamentos', async () => {
    mockedListTransfers.mockResolvedValueOnce([])

    const wrapper = mount(TransferList)
    await flushPromises()

    expect(wrapper.text()).toContain('Nenhum agendamento cadastrado ainda.')
    expect(wrapper.find('table').exists()).toBe(false)
  })

  it('mostra mensagem de erro quando a API falha', async () => {
    mockedListTransfers.mockRejectedValueOnce(new Error('network error'))

    const wrapper = mount(TransferList)
    await flushPromises()

    expect(wrapper.text()).toContain('Não foi possível carregar o extrato')
  })
})
