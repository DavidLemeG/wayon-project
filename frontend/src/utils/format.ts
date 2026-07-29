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

/**
 * Converte o Date do DatePicker para o formato que a API espera (yyyy-MM-dd).
 *
 * Usa os getters locais (getFullYear/getMonth/getDate) em vez de
 * `toISOString().split('T')[0]`: o DatePicker devolve meia-noite **local**, e
 * o toISOString converte para UTC antes de formatar. Em fuso positivo
 * (UTC+2, por exemplo) a meia-noite de 29/07 vira 28/07T22:00Z, e a API
 * receberia o dia anterior — o mesmo erro de `formatDate`, ao contrario.
 */
export function toIsoDate(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function formatCurrency(value: number): string {
  return currencyFormatter.format(value)
}

export function formatPercent(rate: number): string {
  return `${percentFormatter.format(rate * 100)}%`
}
