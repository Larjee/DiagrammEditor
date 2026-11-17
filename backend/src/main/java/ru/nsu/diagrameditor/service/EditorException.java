package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public abstract class EditorException extends RuntimeException {

    private final HttpStatus status;

    protected EditorException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
