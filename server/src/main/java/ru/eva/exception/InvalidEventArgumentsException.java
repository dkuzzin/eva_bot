package ru.eva.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEventArgumentsException extends RuntimeException {
    public InvalidEventArgumentsException(String message) {
        super("Invalid argument: " + message);
    }
}
