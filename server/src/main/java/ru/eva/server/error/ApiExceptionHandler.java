package ru.eva.server.error;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.eva.exception.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiError> handleEventNotFound(EventNotFoundException exception) {
        ApiError error = new ApiError("EVENT_NOT_FOUND", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RegistrationNotAllowedException.class)
    public ResponseEntity<ApiError> handleRegistrationNotAllowed(
            RegistrationNotAllowedException exception
    ) {
        ApiError error = new ApiError(exception.getCode(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RegistrationNotFoundException.class)
    public ResponseEntity<ApiError> handleRegistrationNotFound(RegistrationNotFoundException exception) {
        ApiError error = new ApiError("REGISTRATION_NOT_FOUND", exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidEventArgumentsException.class)
    public ResponseEntity<ApiError> handleInvalidEventArguments(
            InvalidEventArgumentsException exception
    ) {
        ApiError error = new ApiError(
                exception.getCode(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(InvalidRegistrationException.class)
    public ResponseEntity<ApiError> handleInvalidRegistration(
            InvalidRegistrationException exception
    ) {
        ApiError error = new ApiError(exception.getCode(), exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ApiError error = new ApiError("VALIDATION_ERROR", "Request validation failed", fields);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        ApiError error = new ApiError(
                "DATA_INTEGRITY_VIOLATION",
                "Database constraint was violated"
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
}
