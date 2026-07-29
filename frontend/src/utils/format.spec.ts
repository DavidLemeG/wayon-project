import { describe, expect, it } from 'vitest'
import { formatCurrency, formatDate, formatPercent, toIsoDate } from './format'

// Intl usa espaco nao-quebravel (U+00A0) entre "R$" e o numero.
const normalize = (text: string) => text.replace(/\u00A0/g, ' ')

describe('formatDate', () => {
  it('converte data ISO da API para o formato brasileiro', () => {
    expect(formatDate('2026-07-29')).toBe('29/07/2026')
  })

  /**
   * Regressao: `new Date('2026-07-29')` e interpretado como meia-noite UTC,
   * e em UTC-3 renderizaria 28/07/2026 — o dia anterior. Este teste falharia
   * se alguem trocasse a implementacao por Date + toLocaleDateString.
   */
  it('nao desloca o dia por causa de fuso horario', () => {
    expect(formatDate('2026-01-01')).toBe('01/01/2026')
    expect(formatDate('2026-03-01')).toBe('01/03/2026')
    expect(formatDate('2026-12-31')).toBe('31/12/2026')
  })
})

describe('toIsoDate', () => {
  it('converte o Date do DatePicker para o formato da API', () => {
    expect(toIsoDate(new Date(2026, 6, 29))).toBe('2026-07-29')
  })

  it('preenche mes e dia com zero a esquerda', () => {
    expect(toIsoDate(new Date(2026, 0, 5))).toBe('2026-01-05')
  })

  /**
   * Regressao (espelho do caso de formatDate): `toISOString()` converte para
   * UTC antes de formatar, entao a meia-noite local de 29/07 em fuso positivo
   * viraria 28/07T22:00Z e a API receberia o dia anterior. Usar os getters
   * locais mantem o dia escolhido pelo usuario, em qualquer fuso.
   */
  it('preserva o dia escolhido, sem conversao para UTC', () => {
    const escolhido = new Date(2026, 6, 29, 0, 0, 0)
    expect(toIsoDate(escolhido)).toBe('2026-07-29')
    expect(toIsoDate(escolhido)).toBe(
      `${escolhido.getFullYear()}-07-${String(escolhido.getDate()).padStart(2, '0')}`,
    )
  })
})

describe('formatCurrency', () => {
  it('formata com virgula decimal e separador de milhar brasileiro', () => {
    expect(normalize(formatCurrency(222))).toBe('R$ 222,00')
    expect(normalize(formatCurrency(1000))).toBe('R$ 1.000,00')
    expect(normalize(formatCurrency(5555558.55))).toBe('R$ 5.555.558,55')
  })

  it('mantem duas casas decimais mesmo em valor inteiro', () => {
    expect(normalize(formatCurrency(12))).toBe('R$ 12,00')
  })
})

describe('formatPercent', () => {
  it('formata a aliquota com virgula decimal', () => {
    expect(formatPercent(0.025)).toBe('2,50%')
    expect(formatPercent(0.082)).toBe('8,20%')
    expect(formatPercent(0)).toBe('0,00%')
  })
})
