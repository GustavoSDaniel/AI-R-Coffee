package com.gustavosdaniel.aircoffeeapi.exception.handler;

import com.gustavosdaniel.aircoffeeapi.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status, ProblemType type, String detail
    ){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setType(type.getUri());
        problemDetail.setTitle(type.getTitle());
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException exception){

        log.warn("Regra de negócio violada: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ProblemType.BUSINESS_RULE,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex){

        log.warn("Validação falhou para a requisição");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        ResponseEntity<ProblemDetail> response = buildResponse(
                HttpStatus.BAD_REQUEST,
                ProblemType.VALIDATE_ERROR,
                "Um ou mais campos estão inválidos. " +
                        "Faça o preenchimento correto e tente novamente."
        );

        if (response.getBody() != null) {
            response.getBody().setProperty("invalid_fields", errors);
        }

        return response;
    }


}
