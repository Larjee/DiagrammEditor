package ru.nsu.diagrameditor.service;

import org.springframework.http.HttpStatus;

public class NoOpenDiagramException extends EditorException {

    public NoOpenDiagramException() {
        super(HttpStatus.CONFLICT, "No diagram is open for editing");
    }
}
