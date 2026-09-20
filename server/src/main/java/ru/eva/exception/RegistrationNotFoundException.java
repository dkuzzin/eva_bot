package ru.eva.exception;

public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException() {
        super("Registration not found");
    }
}
