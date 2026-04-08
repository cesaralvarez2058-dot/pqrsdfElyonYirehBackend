package com.elyonyireh.pqrsdfelyonyirehbackend.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.RadicarPqrsdfResponseDTO;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RadicarPqrsdfResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Error no manejado capturado desde el frontend. Request: {} - Error: {}", request.getDescription(false), ex.getMessage(), ex);
        return new ResponseEntity<>(new RadicarPqrsdfResponseDTO(false, "Ocurrió un error interno en el servidor: " + ex.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RadicarPqrsdfResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Error de validación en la petición del frontend. Request: {}", request.getDescription(false), ex);
        return new ResponseEntity<>(new RadicarPqrsdfResponseDTO(false, "Datos de petición inválidos", null), HttpStatus.BAD_REQUEST);
    }
}
