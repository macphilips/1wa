package org.oneworldaccuracy.demo.controller.errors;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    protected abstract HttpStatus getStatus();

    BaseException() {
        this("");
    }

    BaseException(String message) {
        super(message);
    }
}
