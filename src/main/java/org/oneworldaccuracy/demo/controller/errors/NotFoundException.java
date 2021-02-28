package org.oneworldaccuracy.demo.controller.errors;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public HttpStatus getStatus() {
        return status;
    }

    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public NotFoundException() {
        this("Record not found!");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
