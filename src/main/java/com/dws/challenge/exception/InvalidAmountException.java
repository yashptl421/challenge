package com.dws.challenge.exception;

public class InvalidAmountException extends RuntimeException {


    public InvalidAmountException() {

    }

    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable throwable) {
        super(message, throwable);
    }
}