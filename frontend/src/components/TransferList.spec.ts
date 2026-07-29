import { describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import TransferList from './TransferList.vue'
import { mountWithPrimeVue } from '../test/mountWithPrimeVue'
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

    const wrapper = mountWithPrimeVue(TransferList)
    await flushPromises()

    // Intl usa espaco nao-quebravel entre "R$" e o numero. Escapado como
    // \u00A0 de proposito: o caractere literal e indistinguivel de um espaco
    // comum no editor, e o replace passa a nao fazer nada sem ninguem notar.
    const row = wrapper.find('tbody tr').text().replace(/\u00A0/g, ' ')
    expect(row).toContain('1111111111')
    expect(row).toContain('2222222222')
    expect(row).toContain('R$ 1.000,00')
    expect(row).toContain('8,20%')
    expect(row).toContain('R$ 82,00')
  })

  it('exibe as datas no formato brasileiro', async () => {
    mockedListTransfers.mockResolvedValueOnce([sampleTransfer])

    const wrapper = mountWithPrimeVue(TransferList)
    await flushPromises()

    const row = wrapper.find('tbody tr')
    expect(row.text()).toContain('12/08/2026')
    expect(row.text()).toContain('28/07/2026')
    expect(row.text()).not.toContain('2026-08-12')
  })

  it('mostra mensagem de vazio quando nao ha agendamentos', async () => {
    mockedListTransfers.mockResolvedValueOnce([])

    const wrapper = mountWithPrimeVue(TransferList)
    await flushPromises()

    expect(wrapper.text()).toContain('Nenhum agendamento cadastrado ainda.')
    // O DataTable sempre renderiza o <table> (a mensagem de vazio vai dentro
    // dele), entao o que importa e nao haver nenhuma linha de dado.
    expect(wrapper.findAll('tbody tr td').some((cell) => /^\d{10}$/.test(cell.text()))).toBe(false)
  })

  it('mostra mensagem de erro quando a API falha', async () => {
    mockedListTransfers.mockRejectedValueOnce(new Error('network error'))

    const wrapper = mountWithPrimeVue(TransferList)
    await flushPromises()

    expect(wrapper.text()).toContain('Não foi possível carregar o extrato')
  })
})
