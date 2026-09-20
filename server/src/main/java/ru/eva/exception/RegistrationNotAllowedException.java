package ru.eva.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class RegistrationNotAllowedException extends RuntimeException {

    private final String code;
    public RegistrationNotAllowedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
