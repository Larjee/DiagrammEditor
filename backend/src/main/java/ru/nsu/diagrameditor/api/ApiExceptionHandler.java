package ru.nsu.diagrameditor.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.nsu.diagrameditor.service.EditorException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EditorException.class)
    public ResponseEntity<ApiError> handle(EditorException e) {
        return ResponseEntity.status(e.getStatus()).body(new ApiError(e.getMessage()));
    }

    public record ApiError(String message) {
    }
}
