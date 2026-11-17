package ru.nsu.diagrameditor.service;

public class SchemaViolationException extends RuntimeException {

    public SchemaViolationException(String message) {
        super(message);
    }
}
