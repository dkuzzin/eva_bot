package ru.eva.exception;

public class EventAlreadyCancelledException extends RuntimeException {

    public EventAlreadyCancelledException(Long eventId) {
        super("Event " + eventId + " is already cancelled");
    }
}