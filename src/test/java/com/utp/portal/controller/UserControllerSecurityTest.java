package com.utp.portal.controller;

import com.utp.portal.config.SecurityConfig;
import com.utp.portal.model.dto.CampusResponse;
import com.utp.portal.model.dto.Role;
import com.utp.portal.model.dto.UserLoginResponse;
import com.utp.portal.model.dto.UserResponse;
import com.utp.portal.service.UserService;
import com.utp.portal.util.security.ApiExceptionSecurityHandler;
import com.utp.portal.util.security.UserReadAuthorizationManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ejercita la cadena de filtros real: el token viaja en la cabecera `Authorization` y solo se
 * simula el decodificador de JWT, de modo que el converter de roles y la regla de autorización
 * de `/users/{id}` se evalúan tal como en producción.
 */
@WebFluxTest(controllers = UserController.class)
@Import({ SecurityConfig.class, UserReadAuthorizationManager.class, ApiExceptionSecurityHandler.class })
class UserControllerSecurityTest {

  private static final String ROLE_STUDENT = "ROLE_STUDENT";
  private static final String ROLE_SAE = "ROLE_SAE";
  private static final String ROLE_SECURITY = "ROLE_SECURITY";
  private static final String ROLE_INTERNAL_SERVICE = "ROLE_INTERNAL_SERVICE";

  @Autowired
  private WebTestClient webTestClient;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private ReactiveJwtDecoder jwtDecoder;

  private String tokenFor(Long userId, String... roles) {
    String tokenValue = "token-" + userId + "-" + String.join("-", roles);
    Jwt.Builder builder = Jwt.withTokenValue(tokenValue)
        .header("alg", "RS256")
        .subject("jdoe")
        .claim("roles", List.of(roles))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600));

    // Un token de servicio no representa a ninguna persona, así que no lleva el claim userId.
    if (userId != null) {
      builder.claim("userId", userId);
    }

    when(jwtDecoder.decode(tokenValue)).thenReturn(Mono.just(builder.build()));
    return tokenValue;
  }

  private UserResponse sampleResponse(Long idUser) {
    return UserResponse.builder()
        .idUser(idUser)
        .username("jdoe")
        .name("Sebastián")
        .lastname("Reyes")
        .dni("12345678")
        .institutionalEmail("jdoe@utp.edu.pe")
        .career("Ingeniería de Sistemas")
        .actualRegistered(true)
        .roles(List.of(new Role(1L, ROLE_STUDENT)))
        .campus(new CampusResponse(10L, "Main Campus"))
        .build();
  }

  @Test
  void getCurrentUser_returns200_whenTokenIsValidAndUserExists() {
    when(userService.findAuthenticated()).thenReturn(Mono.just(sampleResponse(10L)));

    webTestClient.get().uri("/users/me")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_STUDENT))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.idUser").isEqualTo(10)
        .jsonPath("$.username").isEqualTo("jdoe")
        .jsonPath("$.roles[0].name").isEqualTo(ROLE_STUDENT)
        .jsonPath("$.campus.nameCampus").isEqualTo("Main Campus")
        .jsonPath("$.password").doesNotExist();
  }

  @Test
  void getCurrentUser_returns401_whenThereIsNoToken() {
    webTestClient.get().uri("/users/me")
        .exchange()
        .expectStatus().isUnauthorized()
        .expectBody()
        .jsonPath("$.errorType").isEqualTo("FUNCTIONAL")
        .jsonPath("$.description").exists();

    verify(userService, never()).findAuthenticated();
  }

  @Test
  void getCurrentUser_returns404_whenTokenUserNoLongerExists() {
    when(userService.findAuthenticated()).thenReturn(Mono.empty());

    webTestClient.get().uri("/users/me")
        .header("Authorization", "Bearer " + tokenFor(99L, ROLE_STUDENT))
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void getUserById_returns200_whenRequestingOwnId() {
    when(userService.findById(10L)).thenReturn(Mono.just(sampleResponse(10L)));

    webTestClient.get().uri("/users/10")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_STUDENT))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.idUser").isEqualTo(10)
        .jsonPath("$.password").doesNotExist();
  }

  @Test
  void getUserById_returns403_whenRequestingAnotherIdWithoutPrivilegedRole() {
    webTestClient.get().uri("/users/25")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_STUDENT))
        .exchange()
        .expectStatus().isForbidden()
        .expectBody()
        .jsonPath("$.errorType").isEqualTo("FUNCTIONAL");

    verify(userService, never()).findById(anyLong());
  }

  @Test
  void getUserById_returns200_whenPrivilegedRoleRequestsAnotherId() {
    when(userService.findById(25L)).thenReturn(Mono.just(sampleResponse(25L)));

    webTestClient.get().uri("/users/25")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_SAE))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.idUser").isEqualTo(25)
        .jsonPath("$.password").doesNotExist();
  }

  @Test
  void getUserById_returns200_whenSecurityStaffRequestsAnotherId() {
    when(userService.findById(25L)).thenReturn(Mono.just(sampleResponse(25L)));

    webTestClient.get().uri("/users/25")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_SECURITY))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.idUser").isEqualTo(25)
        .jsonPath("$.password").doesNotExist();
  }

  @Test
  void getUserById_returns404_whenPrivilegedRoleRequestsMissingUser() {
    when(userService.findById(99L)).thenReturn(Mono.empty());

    webTestClient.get().uri("/users/99")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_SAE))
        .exchange()
        .expectStatus().isNotFound();
  }

  @Test
  void getUserById_returns401_whenThereIsNoToken() {
    webTestClient.get().uri("/users/10")
        .exchange()
        .expectStatus().isUnauthorized();

    verify(userService, never()).findById(anyLong());
  }

  @Test
  void getUsersByRole_returns200_forAnyAuthenticatedUser() {
    // Lo consulta business-parking-request con el token del estudiante que crea la solicitud,
    // para repartir la revisión entre el Personal SAE del campus.
    when(userService.findByRole(ROLE_SAE, 10L)).thenReturn(Flux.just(sampleResponse(25L)));

    webTestClient.get().uri("/users/by-role/{roleName}?idCampus=10", ROLE_SAE)
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_STUDENT))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[0].idUser").isEqualTo(25)
        .jsonPath("$[0].password").doesNotExist();
  }

  @Test
  void getUsersByRole_returns401_whenThereIsNoToken() {
    webTestClient.get().uri("/users/by-role/{roleName}", ROLE_SAE)
        .exchange()
        .expectStatus().isUnauthorized();

    verify(userService, never()).findByRole(anyString(), anyLong());
  }

  @Test
  void getUserByUsername_returns200WithCredentials_whenCallerIsAnInternalService() {
    UserLoginResponse credentials = UserLoginResponse.builder()
        .idUser(10L)
        .username("jdoe")
        .password("$2a$10$hashedPassword")
        .roles(List.of(new Role(1L, ROLE_STUDENT)))
        .build();
    when(userService.findByUsername("jdoe")).thenReturn(Mono.just(credentials));

    webTestClient.get().uri("/users/username/jdoe")
        .header("Authorization", "Bearer " + tokenFor(null, ROLE_INTERNAL_SERVICE))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.username").isEqualTo("jdoe")
        .jsonPath("$.password").isEqualTo("$2a$10$hashedPassword");
  }

  @Test
  void getUserByUsername_returns401_whenThereIsNoToken() {
    webTestClient.get().uri("/users/username/jdoe")
        .exchange()
        .expectStatus().isUnauthorized();

    verify(userService, never()).findByUsername(anyString());
  }

  @Test
  void getUserByUsername_returns403_whenCallerIsAnEndUser() {
    webTestClient.get().uri("/users/username/jdoe")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_STUDENT))
        .exchange()
        .expectStatus().isForbidden();

    verify(userService, never()).findByUsername(anyString());
  }

  @Test
  void getUserByUsername_returns403_whenCallerIsSaeStaff() {
    // ROLE_SAE puede leer otros usuarios, pero nunca sus credenciales.
    webTestClient.get().uri("/users/username/jdoe")
        .header("Authorization", "Bearer " + tokenFor(10L, ROLE_SAE))
        .exchange()
        .expectStatus().isForbidden();

    verify(userService, never()).findByUsername(anyString());
  }
}
