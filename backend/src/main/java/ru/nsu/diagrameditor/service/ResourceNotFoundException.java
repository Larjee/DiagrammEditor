package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends EditorException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
