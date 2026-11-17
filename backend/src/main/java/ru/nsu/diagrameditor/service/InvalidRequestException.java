package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends EditorException {

    public InvalidRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
