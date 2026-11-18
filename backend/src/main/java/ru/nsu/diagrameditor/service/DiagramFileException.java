package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public class DiagramFileException extends EditorException {

    public DiagramFileException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
