package com.biblioteca.servidor.controller;

import com.biblioteca.servidor.service.ApiException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> tratarRegra(ApiException excecao) {
        return ResponseEntity.status(excecao.getStatus()).body(Map.of("mensagem", excecao.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> tratarCorpoInvalido() {
        return ResponseEntity.badRequest().body(Map.of("mensagem", "dados inválidos"));
    }
}
