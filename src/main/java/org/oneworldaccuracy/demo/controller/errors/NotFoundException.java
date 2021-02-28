package org.oneworldaccuracy.demo.controller.errors;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class NotFoundException extends BaseException {
    private final HttpStatus status = NOT_FOUND;

    public HttpStatus getStatus() {
        return status;
    }

    public NotFoundException() {
        this("Record not found!");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
