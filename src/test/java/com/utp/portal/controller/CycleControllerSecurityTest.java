package com.utp.portal.controller;

import com.utp.portal.config.SecurityConfig;
import com.utp.portal.model.dto.CycleResponse;
import com.utp.portal.service.CycleService;
import com.utp.portal.util.security.ApiExceptionSecurityHandler;
import com.utp.portal.util.security.UserReadAuthorizationManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El ciclo vigente dejó de ser público: `business-parking-request` lo consulta propagando el token
 * del usuario que está creando su solicitud.
 */
@WebFluxTest(controllers = CycleController.class)
@Import({ SecurityConfig.class, UserReadAuthorizationManager.class, ApiExceptionSecurityHandler.class })
class CycleControllerSecurityTest {

  private static final String TOKEN = "cycle-token";

  @Autowired
  private WebTestClient webTestClient;

  @MockitoBean
  private CycleService cycleService;

  @MockitoBean
  private ReactiveJwtDecoder jwtDecoder;

  private String studentToken() {
    Jwt jwt = Jwt.withTokenValue(TOKEN)
        .header("alg", "RS256")
        .subject("jdoe")
        .claim("userId", 10L)
        .claim("roles", List.of("ROLE_STUDENT"))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600))
        .build();
    when(jwtDecoder.decode(TOKEN)).thenReturn(Mono.just(jwt));
    return TOKEN;
  }

  @Test
  void getCurrentCycle_returns200_forAnyAuthenticatedUser() {
    when(cycleService.getCurrentCycle()).thenReturn(Mono.just(new CycleResponse(5L, "2026-1")));

    webTestClient.get().uri("/cycles/current")
        .header("Authorization", "Bearer " + studentToken())
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.nameCycle").isEqualTo("2026-1");
  }

  @Test
  void getCurrentCycle_returns401_whenThereIsNoToken() {
    webTestClient.get().uri("/cycles/current")
        .exchange()
        .expectStatus().isUnauthorized();

    verify(cycleService, never()).getCurrentCycle();
  }

  @Test
  void getCycleById_returns401_whenThereIsNoToken() {
    webTestClient.get().uri("/cycles/5")
        .exchange()
        .expectStatus().isUnauthorized();

    verify(cycleService, never()).findById(anyLong());
  }
}
