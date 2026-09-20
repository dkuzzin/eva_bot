package ru.eva.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long id) {
        super("Event with id " + id + " not found");
    }
}