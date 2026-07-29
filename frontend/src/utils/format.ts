const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
})

const percentFormatter = new Intl.NumberFormat('pt-BR', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/**
 * Converte a data ISO da API (yyyy-MM-dd) para o formato brasileiro.
 *
 * Faz split da string em vez de usar `new Date(iso)` de propósito: o
 * construtor do Date interpreta "2026-07-29" como meia-noite **UTC**, e em
 * fuso negativo (Brasil, UTC-3) o `toLocaleDateString` renderizaria o dia
 * anterior — 28/07/2026. Como aqui a data é um dia de calendário, sem hora
 * nem fuso, tratar como texto é o correto.
 */
export function formatDate(isoDate: string): string {
  const [year, month, day] = isoDate.split('-')
  return `${day}/${month}/${year}`
}

export function formatCurrency(value: number): string {
  return currencyFormatter.format(value)
}

export function formatPercent(rate: number): string {
  return `${percentFormatter.format(rate * 100)}%`
}
