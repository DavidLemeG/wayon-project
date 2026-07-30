import axios from 'axios'

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

/**
 * Timeout explicito: sem ele, uma API que **pendura** (em vez de recusar a
 * conexao) deixa o axios esperando indefinidamente — o botao fica preso em
 * "Agendando..." e o extrato no spinner, sem nunca cair no catch que exibe a
 * mensagem de erro. Com o timeout, a falha vira uma mensagem para o usuario.
 */
const TIMEOUT_MS = 10_000

export const api = axios.create({
  baseURL,
  timeout: TIMEOUT_MS,
  headers: {
    'Content-Type': 'application/json',
  },
})
