package com.wayon.transferscheduling.domain.transfer.exception;

public class InvalidTransferDateException extends TransferValidationException {

    private static final String JANELA_VALIDA =
            "A janela válida é de 0 a 50 dias entre o agendamento e a transferência.";

    public InvalidTransferDateException(long daysBetween) {
        super(buildMessage(daysBetween));
    }

    private static String buildMessage(long daysBetween) {
        if (daysBetween < 0) {
            return String.format(
                    "A data da transferência não pode estar no passado (informada %d dia(s) antes de hoje). %s",
                    Math.abs(daysBetween), JANELA_VALIDA);
        }

        return String.format(
                "Não há taxa aplicável para uma transferência agendada com %d dia(s) de antecedência. %s",
                daysBetween, JANELA_VALIDA);
    }

}
