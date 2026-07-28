package com.wayon.transferscheduling.domain.transfer.exception;

public class SameAccountTransferException extends TransferValidationException {

    public SameAccountTransferException() {
        super("A conta de destino não pode ser a mesma que a conta de origem.");
    }

}
