package com.utp.portal.util.security;

import com.utp.portal.model.dto.ModelApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ApiExceptionSecurityHandler implements ServerAuthenticationEntryPoint, ServerAccessDeniedHandler {

  private static final String ERROR_TYPE_FUNCTIONAL = "FUNCTIONAL";
  private static final String UNAUTHORIZED_DESCRIPTION = "Se requiere un token de autenticación válido";
  private static final String FORBIDDEN_DESCRIPTION = "No tiene permisos para acceder a este recurso";

  private final ObjectMapper objectMapper;

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    return write(exchange, HttpStatus.UNAUTHORIZED, UNAUTHORIZED_DESCRIPTION);
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException ex) {
    return write(exchange, HttpStatus.FORBIDDEN, FORBIDDEN_DESCRIPTION);
  }

  private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String description) {
    exchange.getResponse().setStatusCode(status);
    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ModelApiException body = ModelApiException.builder()
        .description(description)
        .errorType(ERROR_TYPE_FUNCTIONAL)
        .build();

    return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(body))
        .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
        .flatMap(buffer -> exchange.getResponse().writeWith(Mono.just(buffer))
            .doOnError(error -> DataBufferUtils.release(buffer)));
  }
}
