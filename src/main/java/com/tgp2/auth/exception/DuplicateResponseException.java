package com.tgp2.auth.exception;

public class DuplicateResponseException extends RuntimeException {
    public DuplicateResponseException(String message) {
        super(message);
    }
}
