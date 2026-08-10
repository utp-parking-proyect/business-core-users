package com.utp.portal.exception;

import com.utp.portal.model.dto.ApiExceptionDetail;
import com.utp.portal.model.dto.ModelApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String NAME_MICROSERVICE = "business-core-portal";
  private static final String ERROR_TYPE_FUNCTIONAL = "FUNCTIONAL";
  private static final String ERROR_TYPE_TECHNICAL = "TECHNICAL";

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<ModelApiException> handleValidationError(WebExchangeBindException ex) {
    List<ApiExceptionDetail> details = ex.getFieldErrors().stream()
        .map(this::toDetail)
        .toList();

    return ResponseEntity.badRequest().body(ModelApiException.builder()
        .description("Los datos proporcionados no son válidos")
        .errorType(ERROR_TYPE_FUNCTIONAL)
        .exceptionDetails(details)
        .build());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ModelApiException> handleResponseStatusException(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(ModelApiException.builder()
        .description(ex.getReason())
        .errorType(errorTypeFor(ex.getStatusCode()))
        .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ModelApiException> handleUnexpectedError(Exception ex) {
    log.error("Unexpected error", ex);

    return ResponseEntity.internalServerError().body(ModelApiException.builder()
        .description("Ocurrió un error inesperado")
        .errorType(ERROR_TYPE_TECHNICAL)
        .exceptionDetails(List.of(new ApiExceptionDetail(NAME_MICROSERVICE, ex.getMessage())))
        .build());
  }

  private ApiExceptionDetail toDetail(FieldError fieldError) {
    return new ApiExceptionDetail(fieldError.getField(), fieldError.getDefaultMessage());
  }

  private String errorTypeFor(HttpStatusCode status) {
    return status.is5xxServerError() ? ERROR_TYPE_TECHNICAL : ERROR_TYPE_FUNCTIONAL;
  }
}
