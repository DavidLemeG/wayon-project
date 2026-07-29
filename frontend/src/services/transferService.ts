import { api } from './api'
import type { TransferRequest, TransferResponse } from './transferTypes'

export async function createTransfer(request: TransferRequest): Promise<TransferResponse> {
  const response = await api.post<TransferResponse>('/api/transfers', request)
  return response.data
}

export async function listTransfers(): Promise<TransferResponse[]> {
  const response = await api.get<TransferResponse[]>('/api/transfers')
  return response.data
}
