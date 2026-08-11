package com.utp.portal.util.security;

import com.utp.portal.util.constants.Constants;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class AuthenticatedUserProvider {

  public Mono<Long> getAuthenticatedUserId() {
    return ReactiveSecurityContextHolder.getContext()
        .map(context -> Objects.requireNonNull(context.getAuthentication()).getPrincipal())
        .cast(Jwt.class)
        .map(jwt -> jwt.getClaim(Constants.CLAIM_USER_ID))
        .cast(Number.class)
        .map(Number::longValue);
  }
}
