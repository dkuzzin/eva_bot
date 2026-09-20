package ru.eva.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidEventArgumentsException extends RuntimeException {
    private final String code;

    public InvalidEventArgumentsException(String code, String message) {
        super("Invalid argument: " + message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
