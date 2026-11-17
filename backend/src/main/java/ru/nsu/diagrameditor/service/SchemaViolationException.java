package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public class SchemaViolationException extends EditorException {

    public SchemaViolationException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
