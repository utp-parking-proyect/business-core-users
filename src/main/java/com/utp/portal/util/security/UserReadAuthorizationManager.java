package com.utp.portal.util.security;

import com.utp.portal.util.constants.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserReadAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

  private static final String PATH_VARIABLE_ID = "id";

  @Override
  public Mono<AuthorizationResult> authorize(Mono<Authentication> authentication,
                                             AuthorizationContext context) {
    Object requestedId = context.getVariables().get(PATH_VARIABLE_ID);

    return authentication
        .filter(Authentication::isAuthenticated)
        .map(auth -> new AuthorizationDecision(canReadAnyUser(auth) || isOwner(auth, requestedId)))
        .cast(AuthorizationResult.class)
        .defaultIfEmpty(new AuthorizationDecision(false));
  }

  private boolean canReadAnyUser(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .anyMatch(Constants.ROLES_ALLOWED_TO_READ_ANY_USER::contains);
  }

  private boolean isOwner(Authentication authentication, Object requestedId) {
    if (requestedId == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
      return false;
    }
    Object userId = jwt.getClaim(Constants.CLAIM_USER_ID);
    return userId instanceof Number number && String.valueOf(number.longValue()).equals(requestedId.toString());
  }
}
