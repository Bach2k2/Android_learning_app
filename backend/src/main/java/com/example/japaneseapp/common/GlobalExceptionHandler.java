package com.example.japaneseapp.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/***
 * Global exception handler for the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ApiException and returns a structured error response.
     *
     * @param ex  the ApiException thrown
     * @param req the HttpServletRequest that resulted in the exception
     * @return a ResponseEntity containing the ApiError
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest req) {
        var status = ex.getStatus();
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), req.getRequestURI(), null));
    }

    /** 
     * Handles MethodArgumentNotValidException and returns a structured error response with field validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, Object> details = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        details.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", "Validation failed", req.getRequestURI(), details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", "Validation failed", req.getRequestURI(), null));
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleInvalidRequest(Exception ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", "Invalid request", req.getRequestURI(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Unexpected error", req.getRequestURI(), null));
    }
}
