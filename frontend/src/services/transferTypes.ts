export interface TransferRequest {
  originAccount: string
  destinationAccount: string
  amount: number
  transferDate: string // yyyy-MM-dd
}

export interface TransferResponse {
  id: number
  originAccount: string
  destinationAccount: string
  amount: number
  fixedFee: number
  percentageRate: number
  percentageFee: number
  totalFee: number
  transferDate: string
  schedulingDate: string
}

// Espelha ApiError no backend (GlobalExceptionHandler).
export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: string[] | null
}
