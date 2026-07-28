package com.wayon.transferscheduling.domain.transfer.exception;

public abstract class TransferValidationException extends RuntimeException {

    protected TransferValidationException(String message) {
        super(message);
    }

}
