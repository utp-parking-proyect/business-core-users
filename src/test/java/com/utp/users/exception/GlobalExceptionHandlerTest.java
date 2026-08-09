package com.utp.users.exception;

import com.utp.users.model.dto.ModelApiException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleResponseStatusException_mapsStatusReasonAndFunctionalErrorType() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "El username ya está registrado");

    ResponseEntity<ModelApiException> response = handler.handleResponseStatusException(ex);

    assert response.getStatusCode() == HttpStatus.CONFLICT;
    assert response.getBody() != null;
    assert response.getBody().getDescription().equals("El username ya está registrado");
    assert response.getBody().getErrorType().equals("FUNCTIONAL");
  }

  @Test
  void handleResponseStatusException_marksServerErrorsAsTechnical() {
    ResponseStatusException ex = new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "boom");

    ResponseEntity<ModelApiException> response = handler.handleResponseStatusException(ex);

    assert response.getBody() != null;
    assert response.getBody().getErrorType().equals("TECHNICAL");
  }

  @Test
  void handleValidationError_mapsFieldErrorsToExceptionDetails() throws NoSuchMethodException {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "userRegisterRequest");
    bindingResult.addError(new FieldError("userRegisterRequest", "username", "must not be blank"));
    MethodParameter methodParameter = new MethodParameter(
        GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class), 0);
    WebExchangeBindException ex = new WebExchangeBindException(methodParameter, bindingResult);

    ResponseEntity<ModelApiException> response = handler.handleValidationError(ex);

    assert response.getStatusCode() == HttpStatus.BAD_REQUEST;
    assert response.getBody() != null;
    assert response.getBody().getExceptionDetails().size() == 1;
    assert response.getBody().getExceptionDetails().get(0).getComponent().equals("username");
    assert response.getBody().getExceptionDetails().get(0).getDescription().equals("must not be blank");
  }

  @Test
  void handleUnexpectedError_returns500WithTechnicalErrorTypeAndComponent() {
    ResponseEntity<ModelApiException> response = handler.handleUnexpectedError(new RuntimeException("boom"));

    assert response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR;
    assert response.getBody() != null;
    assert response.getBody().getErrorType().equals("TECHNICAL");
    assert response.getBody().getExceptionDetails().get(0).getComponent().equals("business-core-users");
  }

  private void dummyMethod(String param) {
  }
}
