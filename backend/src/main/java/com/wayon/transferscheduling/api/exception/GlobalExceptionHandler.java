package com.wayon.transferscheduling.api.exception;

import com.wayon.transferscheduling.domain.transfer.exception.TransferValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex,
                                                       HttpServletRequest request) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        ApiError apiError = apiError(HttpStatus.BAD_REQUEST,
                "Erro de validação nos campos enviados.", request)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(TransferValidationException.class)
    public ResponseEntity<ApiError> handleTransferValidation(TransferValidationException ex,
                                                               HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(apiError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request).build());
    }

    /**
     * Corpo malformado (JSON invalido, data em formato nao reconhecido, tipo
     * incompativel). Sem este handler a resposta cairia no formato padrao do
     * Spring, que nao traz "message" — quebrando o contrato unico de erro.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiError(HttpStatus.BAD_REQUEST,
                        "Corpo da requisição inválido: verifique o formato do JSON e dos campos enviados "
                                + "(datas devem usar o formato yyyy-MM-dd).",
                        request).build());
    }

    /**
     * Rede de seguranca: qualquer falha nao prevista tambem responde no formato
     * ApiError, sem vazar stack trace para o cliente (fica so no log).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado ao processar {}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno ao processar a requisição.", request).build());
    }

    private ApiError.ApiErrorBuilder apiError(HttpStatus status, String message,
                                                HttpServletRequest request) {
        return ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI());
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

}
