
package br.com.minispring.exemplo.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        var body = errorBody(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                request
        );

        var details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null
                                ? "Valor inválido"
                                : error.getDefaultMessage()
                ))
                .toList();

        body.put("details", details);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                errorBody(
                        HttpStatus.BAD_REQUEST,
                        "Corpo da requisição inválido",
                        request
                )
        );
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            Exception exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(
                errorBody(
                        HttpStatus.BAD_REQUEST,
                        "Parâmetro inválido",
                        request
                )
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                errorBody(
                        HttpStatus.NOT_FOUND,
                        "Recurso não encontrado",
                        request
                )
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request
    ) {
        var headers = new HttpHeaders();

        if (exception.getSupportedHttpMethods() != null) {
            headers.setAllow(exception.getSupportedHttpMethods());
        }

        return new ResponseEntity<>(
                errorBody(
                        HttpStatus.METHOD_NOT_ALLOWED,
                        "Método HTTP não permitido",
                        request
                ),
                headers,
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE
        ).body(
                errorBody(
                        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                        "Tipo de conteúdo não suportado",
                        request
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        HttpStatusCode status = exception.getStatusCode();

        var body = new LinkedHashMap<String, Object>();

        body.put("status", status.value());
        body.put("message", exception.getReason());
        body.put("requestId", request.getAttribute("requestId"));

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
        ).body(
                errorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno do servidor",
                        request
                )
        );
    }

    private Map<String, Object> errorBody(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        var body = new LinkedHashMap<String, Object>();

        body.put("status", status.value());
        body.put("message", message);
        body.put("requestId", request.getAttribute("requestId"));

        return body;
    }
}
