package org.oneworldaccuracy.demo.controller.errors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is global error handler config class.
 */
@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * This method teaches spring boot how to handle errors that implement/extends BaseException.
     * This provides us with a standard error response data for our controller.
     */
    @ExceptionHandler(value = {BadRequestException.class, NotFoundException.class})
    protected ResponseEntity<Object> handleBadRequest(BaseException ex, NativeWebRequest request) {
        ResponseError responseError = new ResponseError().detail(ex.getMessage()).status(ex.getStatus().value());
        return handleExceptionInternal(
            ex,
            responseError,
            new HttpHeaders(),
            ex.getStatus(),
            request
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM( f.getField(), f.getDefaultMessage()))
            .collect(Collectors.toList());

        HashMap<String, Object> errors = new HashMap<>();
        errors.put("type", "Constraint Violation");
        errors.put("title", "Method argument not valid");
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("detail", "Unable to parse body of request");
        errors.put("fieldErrors", fieldErrors);
        return handleExceptionInternal(
            ex,
            errors,
            new HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request
        );
    }
}
