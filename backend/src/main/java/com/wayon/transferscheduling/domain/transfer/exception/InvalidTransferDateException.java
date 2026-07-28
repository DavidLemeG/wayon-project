package com.wayon.transferscheduling.domain.transfer.exception;

public class InvalidTransferDateException extends TransferValidationException {

    public InvalidTransferDateException(long daysBetween) {
        super(String.format(
                "Não há taxa aplicável para uma transferência agendada com %d dia(s) de antecedência. "
                        + "A janela válida é de 0 a 50 dias entre o agendamento e a transferência.",
                daysBetween));
    }

}
