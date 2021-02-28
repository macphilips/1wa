package org.oneworldaccuracy.demo.controller.errors;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public HttpStatus getStatus() {
        return status;
    }

    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public BadRequestException() {
        this("Bad request!");
    }

    public BadRequestException(String message) {
        super(message);
    }
}
