package ru.eva.exception;

public class EventAccessDeniedException extends RuntimeException {
    public EventAccessDeniedException(Long id) {
        super("User does not own event with id " + id);
    }
}
