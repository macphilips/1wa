package org.oneworldaccuracy.demo.controller.errors;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BadRequestException extends BaseException {
    private final HttpStatus status = BAD_REQUEST;

    public HttpStatus getStatus() {
        return status;
    }

    public BadRequestException() {
        this("Bad request!");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
