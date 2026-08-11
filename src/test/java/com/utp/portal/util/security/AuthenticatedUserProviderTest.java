package com.utp.portal.util.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

class AuthenticatedUserProviderTest {

  private final AuthenticatedUserProvider provider = new AuthenticatedUserProvider();

  private JwtAuthenticationToken tokenWithUserIdClaim(Object userId) {
    Jwt.Builder builder = Jwt.withTokenValue("token")
        .header("alg", "RS256")
        .subject("jdoe")
        .claim("roles", List.of("ROLE_STUDENT"))
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(3600));

    if (userId != null) {
      builder.claim("userId", userId);
    }

    return new JwtAuthenticationToken(builder.build(), List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
  }

  @Test
  void getAuthenticatedUserId_readsUserIdClaim() {
    StepVerifier.create(provider.getAuthenticatedUserId()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(tokenWithUserIdClaim(10L))))
        .expectNext(10L)
        .verifyComplete();
  }

  @Test
  void getAuthenticatedUserId_readsUserIdClaim_whenJsonDecodesItAsInteger() {
    StepVerifier.create(provider.getAuthenticatedUserId()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(tokenWithUserIdClaim(10))))
        .expectNext(10L)
        .verifyComplete();
  }

  @Test
  void getAuthenticatedUserId_completesEmpty_whenThereIsNoSecurityContext() {
    StepVerifier.create(provider.getAuthenticatedUserId())
        .verifyComplete();
  }
}
