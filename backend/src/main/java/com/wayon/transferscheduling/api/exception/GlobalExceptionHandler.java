package com.wayon.transferscheduling.api.exception;

import com.wayon.transferscheduling.domain.transfer.exception.TransferValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Traduz toda falha para um unico formato de resposta ({@link ApiError}).
 *
 * <p>Estende {@link ResponseEntityExceptionHandler} de proposito: ele ja mapeia
 * as excecoes que o proprio Spring MVC lanca para sinalizar erro do cliente
 * (metodo nao suportado -> 405, Content-Type invalido -> 415, rota inexistente
 * -> 404) com o status correto. Sem essa heranca, o @ExceptionHandler generico
 * de Exception capturava essas excecoes e devolvia 500 — alem de registrar um
 * ERROR com stack trace para o que era so um cliente usando o verbo errado,
 * exatamente o ruido que a ADR 0011 diz que ERROR deve evitar.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Validacao de campo (Bean Validation) -> 400 com a lista de campos invalidos.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                    HttpHeaders headers,
                                                                    HttpStatus status,
                                                                    WebRequest request) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.toList());

        log.warn("400 em {}: campos invalidos {}", pathOf(request), fieldErrors);

        ApiError apiError = apiError(HttpStatus.BAD_REQUEST,
                "Erro de validação nos campos enviados.", pathOf(request))
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(apiError);
    }

    /**
     * Corpo malformado (JSON invalido, data em formato nao reconhecido, tipo
     * incompativel). Loga a causa tecnica, util para depurar o cliente, mas nao
     * a devolve na resposta — detalhe interno de parsing nao ajuda quem chama.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                    HttpHeaders headers,
                                                                    HttpStatus status,
                                                                    WebRequest request) {
        log.warn("400 em {}: corpo malformado — {}", pathOf(request),
                ex.getMostSpecificCause().getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers)
                .body(apiError(HttpStatus.BAD_REQUEST,
                        "Corpo da requisição inválido: verifique o formato do JSON e dos campos enviados "
                                + "(datas devem usar o formato yyyy-MM-dd).",
                        pathOf(request)).build());
    }

    /**
     * Demais excecoes tratadas pela classe pai (405, 415, 404, 406...). Preserva
     * os headers que o Spring monta — o "Allow" de um 405, por exemplo, e como o
     * cliente descobre quais metodos a rota aceita.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                              @Nullable Object body,
                                                              HttpHeaders headers,
                                                              HttpStatus status,
                                                              WebRequest request) {
        log.warn("{} em {}: {}", status.value(), pathOf(request), ex.getMessage());

        return ResponseEntity.status(status).headers(headers)
                .body(apiError(status, messageFor(status), pathOf(request)).build());
    }

    @ExceptionHandler(TransferValidationException.class)
    public ResponseEntity<ApiError> handleTransferValidation(TransferValidationException ex,
                                                               HttpServletRequest request) {
        // WARN, nao ERROR: e uma rejeicao esperada por regra de negocio, o sistema
        // funcionou como deveria. Mas fica visivel no log para investigar um pico
        // de rejeicoes (ex.: front enviando data em formato errado).
        log.warn("422 em {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(apiError(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(),
                        request.getRequestURI()).build());
    }

    /**
     * Rede de seguranca para o que nao e previsto por nenhum handler acima:
     * responde no formato ApiError, sem vazar stack trace para o cliente (fica
     * so no log, e aqui sim em ERROR, porque e falha de verdade).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado ao processar {}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiError(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno ao processar a requisição.", request.getRequestURI()).build());
    }

    private String messageFor(HttpStatus status) {
        switch (status) {
            case METHOD_NOT_ALLOWED:
                return "Método HTTP não suportado para este recurso.";
            case UNSUPPORTED_MEDIA_TYPE:
                return "Formato de conteúdo não suportado. Utilize application/json.";
            case NOT_ACCEPTABLE:
                return "Nenhum formato de resposta aceito pelo cliente é suportado. "
                        + "Esta API responde em application/json.";
            case NOT_FOUND:
                return "Recurso não encontrado.";
            default:
                return status.getReasonPhrase();
        }
    }

    private ApiError.ApiErrorBuilder apiError(HttpStatus status, String message, String path) {
        return ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path);
    }

    private String pathOf(WebRequest request) {
        return request instanceof ServletWebRequest
                ? ((ServletWebRequest) request).getRequest().getRequestURI()
                : request.getDescription(false);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

}
