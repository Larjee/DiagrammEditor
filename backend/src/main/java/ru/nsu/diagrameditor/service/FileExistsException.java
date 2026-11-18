package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public class FileExistsException extends EditorException {

    public FileExistsException(String path) {
        super(HttpStatus.CONFLICT, "File '" + path + "' already exists. Confirm to overwrite it");
    }
}
