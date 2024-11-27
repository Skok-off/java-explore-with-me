package ru.practicum.exceptions;

import lombok.Getter;

@Getter
public class DateTimeException extends RuntimeException {
    public DateTimeException(String message) {
        super(message);
    }
}