package ru.practicum.exceptions;

public class BadParamException extends RuntimeException {
    public BadParamException(String message) {
        super(message);
    }
}
