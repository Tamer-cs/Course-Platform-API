package com.courseplatform.api.exception;

import com.courseplatform.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyEnrolledException.class)
    public ResponseEntity<ErrorResponse> alreadyEnrolled(AlreadyEnrolledException ex) {
        return build(HttpStatus.CONFLICT, "AlreadyEnrolledException", ex.getMessage());
    }

    @ExceptionHandler(NotEnrolledException.class)
    public ResponseEntity<ErrorResponse> notEnrolled(NotEnrolledException ex) {
        return build(HttpStatus.FORBIDDEN, "NotEnrolledException", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "ResourceNotFoundException", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse(ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "MethodArgumentNotValidException", msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "AccessDeniedException", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> badCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, "BadCredentialsException", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> fallback(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "InternalServerError", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String error, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .error(error)
                .message(message == null ? "" : message)
                .timestamp(Instant.now().toString())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
